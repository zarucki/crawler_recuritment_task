import java.io.PrintWriter

import cats.effect._
import config.{CliOptionParser, Config}
import extract._
import extract.fetch.Https4Client
import extract.parse.jsoup.JSoupParser
import extract.profiles.{BashOrgContent, BashOrgProfile}
import io.circe.generic.auto._
import io.circe.syntax._
import org.apache.logging.log4j.{Level, LogManager}
import org.apache.logging.log4j.core.config.Configurator
import pureconfig.generic.auto._

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

    extractor
      .fetchAndExtractData(numberOfPagesToFetch, BashOrgProfile, httpClient, htmlParser)
      .map(_.take(config.postCount))
      .map(_.asJson.toString())
      .flatMap(contentToWrite => writeToFile(config.outputPath, contentToWrite).compile.drain)
      .attempt
      .unsafeRunSync()
      .left
      .foreach { throwable =>
        logger.fatal("Crashed with error.", throwable)
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

  private def writeToFile(fileName: String, contentToWrite: String) = {
    // TODO: `handleErrors
    // TODO: write more stream like?
    fs2.Stream.bracket(IO(new PrintWriter(fileName)))(
      use = pw => fs2.Stream.eval(IO(pw.println(contentToWrite))),
      release = pw => IO(pw.close())
    )
  }
}
