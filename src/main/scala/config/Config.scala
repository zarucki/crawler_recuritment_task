package config

case class Config(
    postCount: Int = 5,
    verbose: Boolean = false,
    debug: Boolean = false,
    outputPath: String = "output.json"
) {
  override def toString: String = {
    s"CliConfig(postCount = $postCount, verbose = $verbose, debug = $debug, outputPath = $outputPath)"
  }
}
