package config
import scopt.OptionParser

object CliOptionParser {
  def parser(): OptionParser[Config] = new OptionParser[Config]("run") {
    opt[Int]('n', "pageNumber")
      .action((x, c) => c.copy(pageCount = x))
      .text("number of pages from bash.org.pl/latest to fetch.")

    opt[String]('o', "output")
      .action((s, c) => c.copy(outputPath = s))
      .text("Output path for file.")

    opt[Unit]("debug")
      .action((_, c) => c.copy(debug = true))
      .text("for showing debug messages")

    help("help").text("prints this usage text")

    arg[Int]("<postCount>...")
      .unbounded()
      .optional()
      .action((x, c) => c.copy(pageCount = x))
      .text("optional unbounded args")
  }
}
