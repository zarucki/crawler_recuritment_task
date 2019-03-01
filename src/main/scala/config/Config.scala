package config

case class Config(
    pageCount: Int = 5,
    verbose: Boolean = false,
    debug: Boolean = false,
    outputPath: String = "output.json"
) {
  override def toString: String = {
    s"CliConfig(pageCount = $pageCount, verbose = $verbose, debug = $debug, outputPath = $outputPath)"
  }
}
