name := "crawler_recruitment_task"

version := "0.1"

scalaVersion := "2.12.8"

val log4jVersion = "2.11.2"

// Old because of conflicts in dependencies
val log4CatVersion = "0.1.1"

libraryDependencies ++= Seq(
	"org.typelevel" %% "cats-core" % "1.4.0" withSources() withJavadoc(),
	"org.typelevel" %% "cats-effect" % "0.10.1" withSources() withJavadoc(),
	"com.github.scopt" %% "scopt" % "3.7.1",
	"com.github.pureconfig" %% "pureconfig" % "0.10.2",
	"org.http4s" %% "http4s-blaze-client" % "0.18.22",
	"org.jsoup" % "jsoup" % "1.11.3",
	"org.apache.logging.log4j" % "log4j-slf4j-impl" % log4jVersion,
	"org.apache.logging.log4j" % "log4j-api" % log4jVersion,
	"org.apache.logging.log4j" % "log4j-core" % log4jVersion,
	"io.chrisdavenport" %% "log4cats-core"    % log4CatVersion,
	"io.chrisdavenport" %% "log4cats-slf4j"   % log4CatVersion,
	"io.chrisdavenport" %% "log4cats-log4s"   % log4CatVersion

)

scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint", "-opt:l:inline", "-opt-inline-from:**", "-Ypartial-unification", "-language:higherKinds")

javacOptions ++= Seq("-Xlint")
