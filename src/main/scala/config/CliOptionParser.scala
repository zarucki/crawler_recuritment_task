package config
import scopt.OptionParser

object CliOptionParser {
  def parser(): OptionParser[Config] = new OptionParser[Config]("run") {
    opt[Int]('n', "postCount")
      .action((x, c) => c.copy(postCount = x))
      .text("count of bash.org.pl posts to fetch.")

    opt[Unit]("debug")
      .action((_, c) => c.copy(debug = true))
      .text("for showing debug messages")

    help("help").text("prints this usage text")

    arg[Int]("<postCount>...")
      .unbounded()
      .optional()
      .action((x, c) => c.copy(postCount = x))
      .text("optional unbounded args")
  }
}
