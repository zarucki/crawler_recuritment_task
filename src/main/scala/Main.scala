import cats.data.EitherT
import cats.effect._
import config.{CliOptionParser, Config}
import extract._
import extract.fetch.Http4sClient
import extract.parse.jsoup.JSoupParser
import extract.profiles.{BashOrgContent, BashOrgProfile}
import io.chrisdavenport.log4cats.slf4j.Slf4jLogger
import io.circe.generic.auto._
import org.apache.logging.log4j.{Level, LogManager}
import org.apache.logging.log4j.core.config.Configurator
import pureconfig.generic.auto._
import transform.{CirceJsonSerializer, PrintWriterFileWriter}

object Main extends App {
  private val logger = LogManager.getLogger

  val fileBasedConfig = readFileConfig().getOrElse(Config())

  CliOptionParser.parser().parse(args, fileBasedConfig).foreach { config =>
    if (config.debug) {
      Configurator.setRootLevel(Level.DEBUG)
    }

    logger.info(s"Using config: $config")

    val extractor = new DomainProfileExtractor[IO, BashOrgContent]()
    val httpClient = Http4sClient.apply[IO]()
    val htmlParser = new JSoupParser[IO]()
    val jsonSerializer = new CirceJsonSerializer[IO, BashOrgContent]()

    val fileWriter = new PrintWriterFileWriter[IO]()

    val pipeline = for {
      safeLogger       <- Slf4jLogger.create[EitherT[IO, Throwable, ?]]
      bashContentItems <- extractor.fetchAndExtractData(config.pageCount, BashOrgProfile, httpClient, htmlParser)
      _                <- safeLogger.info(s"Got in total: ${bashContentItems.size} items")
      jsonToWrite      <- jsonSerializer.arrayAsJson(bashContentItems)
      result           <- fileWriter.writeToFile(config.outputPath, jsonToWrite.toString())
    } yield result

    pipeline.value
      .flatMap {
        case Left(throwable) => IO(logger.error("Crashed with error.", throwable))
        case _               => IO(logger.info("Seems like everything worked. Shutting down"))
      }
      .unsafeRunSync()
  }

  private def readFileConfig(): Option[Config] = {
    pureconfig.loadConfig[Config] match {
      case Right(fileConfig) => Some(fileConfig)
      case Left(failures) =>
        logger.error("Failure when reading file config: " + failures)
        None
    }
  }
}
