import java.io.PrintWriter

import cats.effect._
import cats.implicits._
import config.{CliOptionParser, Config}
import extract._
import extract.fetch.Https4Client
import extract.parse.jsoup.JSoupParser
import extract.profiles.{BashOrgContent, BashOrgProfile}
import io.circe.generic.auto._
import org.apache.logging.log4j.{Level, LogManager}
import org.apache.logging.log4j.core.config.Configurator
import pureconfig.generic.auto._
import transform.CirceJsonSerializer
import fs2.{Stream => FStream}

object Main extends App {
  private val logger = LogManager.getLogger

  val fileBasedConfig = readFileConfig().getOrElse(Config())

  CliOptionParser.parser().parse(args, fileBasedConfig).foreach { config =>
    if (config.debug) {
      Configurator.setRootLevel(Level.DEBUG)
    }

    logger.info(s"Using config: $config")

    val numberOfPagesToFetch = Math.ceil(config.postCount / BashOrgProfile.itemsPerPage.toDouble).toInt
    val extractor = new DomainProfileExtractor[IO, BashOrgContent]()
    val httpClient = Https4Client.apply[IO]()
    val htmlParser = new JSoupParser
    val jsonSerializer = new CirceJsonSerializer[IO, BashOrgContent]()

    val pipeline = for {
      bashContentItems <- extractor
        .fetchAndExtractData(numberOfPagesToFetch, BashOrgProfile, httpClient, htmlParser)
      jsonToWrite <- jsonSerializer.arrayAsJson(bashContentItems.take(config.postCount))
      afterWrite <- writeToFile(config.outputPath, jsonToWrite.toString()).compile.drain
    } yield afterWrite

    try {
      pipeline.attempt
        .flatMap {
          case Left(throwable) => IO(logger.error("Crashed with error.", throwable))
          case _               => IO(logger.info("Seems like everything worked. Shutting down"))
        }
        .unsafeRunSync()
    } catch {
      case ex: Exception => logger.fatal("This should not happen, errors should propagate other way.", ex)
    }
  }

  private def readFileConfig(): Option[Config] = {
    pureconfig.loadConfig[Config] match {
      case Right(fileConfig) => Some(fileConfig)
      case Left(failures) =>
        logger.error("Failure when reading file config: " + failures)
        None
    }
  }

  private def writeToFile(fileName: String, contentToWrite: String): FStream[IO, Unit] = {
    val acquirePrinter = IO(Either.catchNonFatal(new PrintWriter(fileName)))

    def writeAction(pw: Either[Throwable, PrintWriter]): FStream[IO, Unit] = {
      FStream.eval(IO[Unit](pw.map(_.println(contentToWrite))))
    }
    def releaseAction(pw: Either[Throwable, PrintWriter]): IO[Unit] = {
      IO[Unit](pw.map(_.close()))
    }

    fs2.Stream.bracket(acquirePrinter)(use = writeAction, release = releaseAction)
  }
}
