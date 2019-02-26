object Main extends App {
  parser.parse(args, CliConfig()) match {
    case Some(config) => println(config)
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

		opt[Unit]("debug").hidden().action( (_, c) =>
			c.copy(debug = true) ).text("this option is hidden in the usage text")

		help("help").text("prints this usage text")

		arg[Int]("<postCount>...")
			.unbounded().optional()
			.action( (x, c) => c.copy(postCount = x) )
			.text("optional unbounded args")
	}

	private case class CliConfig(postCount: Int = 5, verbose: Boolean = false, debug: Boolean = false)
}
