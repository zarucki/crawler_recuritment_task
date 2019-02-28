import java.io.PrintWriter

import cats.effect._
import config.{CliConfig, CliOptionParser, FileConfig}
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
  val logger = LogManager.getLogger

  CliOptionParser.parser().parse(args, CliConfig()) match {
    case Some(config) =>
      val parsedFileConfig =
        pureconfig.loadConfig[FileConfig] match {
          case Right(fileConfig) => fileConfig
          case Left(failures) =>
            logger.error("Failure when reading file config: " + failures)
            FileConfig()
        }

      if (config.debug) {
        Configurator.setRootLevel(Level.DEBUG)
      }

      // TODO: merge two configs into one
      logger.info("CLI config: " + config)
      logger.info("Typesafe config: " + parsedFileConfig)

      val extractedData =
        new DomainProfileExtractor[IO, BashOrgContent]()
          .fetchAndExtractData(
            numberOfPages = Math.ceil(config.postCount / BashOrgProfile.itemsPerPage.toDouble).toInt,
            BashOrgProfile,
            Https4Client.apply[IO](),
            new JSoupParser
          )

      val dataAsJson = extractedData.map(_.take(config.postCount).asJson.toString())

      val dataWrittenToFile =
        dataAsJson.flatMap(contentToWrite => writeToFile(parsedFileConfig.outputPath, contentToWrite).compile.drain)

      dataWrittenToFile.attempt.unsafeRunSync().left.foreach { throwable =>
        logger.fatal("Crashed.", throwable)
      }

    case None => // arguments are bad, error message will have been displayed
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
