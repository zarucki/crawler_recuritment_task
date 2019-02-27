import cats.effect._
import config.{CliConfig, FileConfig}
import extract._
import extract.DomainProfile._
import extract.fetch.Http4sHttpFetcher
import extract.parse._
import extract.parse.jsoup.JSoupParser
import pureconfig.generic.auto._

// TODO: maybe read it from config?
object BashOrgProfile
    extends DomainProfile(
      urlPattern = "http://bash.org.pl/latest/?page=%d",
      mainCssSelector = ".post",
      relativeDetailCssSelectors = Seq(
        HtmlValueExtractor(None, Attribute("id")),
        HtmlValueExtractor(Some(".points"), Text),
        HtmlValueExtractor(Some(".post-content"), InnerHtml)
      )
    )

object Main extends App {

  parser.parse(args, CliConfig()) match {
    case Some(config) =>
      val parsedFileConfig =
        pureconfig.loadConfig[FileConfig] match {
          case Right(fileConfig) => fileConfig
          case Left(failures) =>
            if (config.debug) {
              // TODO: don't use println
              println("failures: " + failures)
            }
            FileConfig()
        }

      println(config)
      println(parsedFileConfig)

      val extractor =
        new DomainProfileExtractor[IO]()
          .extractData(numberOfPages = Math.ceil(config.postCount / 20.0).toInt,
                       BashOrgProfile,
                       new Http4sHttpFetcher[IO](),
                       new JSoupParser)

      val results: Seq[Seq[String]] = extractor.unsafeRunSync()
//      println(results.take(config.postCount).mkString("\n=====\n"))
      println("total: " + results.size)

    case None => // arguments are bad, error message will have been displayed
  }

  private def parser = new scopt.OptionParser[CliConfig]("bash_org_pl_fetch") {
    head("bash_org_pl_fetch", "0.1")

    opt[Int]('n', "postCount")
      .action((x, c) => c.copy(postCount = x))
      .text("count of bash.org.pl posts to fetch.")

    opt[Unit]("verbose")
      .action((_, c) => c.copy(verbose = true))
      .text("verbose is a flag")

    opt[Unit]("debug")
      .hidden()
      .action((_, c) => c.copy(debug = true))
      .text("this option is hidden in the usage text")

    help("help").text("prints this usage text")

    arg[Int]("<postCount>...")
      .unbounded()
      .optional()
      .action((x, c) => c.copy(postCount = x))
      .text("optional unbounded args")
  }
}
