name := "crawler_recruitment_task"

version := "0.1"

scalaVersion := "2.12.8"

val log4jVersion = "2.11.2"
val log4CatVersion = "0.1.1" // Old because of conflicts in dependencies
val scalaTestVersion = "3.0.5"
val circeVersion = "0.10.0"

libraryDependencies ++= Seq(
	"org.typelevel" %% "cats-core" % "1.4.0" withSources() withJavadoc(),
	"org.typelevel" %% "cats-effect" % "0.10.1" withSources() withJavadoc(),
	"com.github.scopt" %% "scopt" % "3.7.1",
	"com.github.pureconfig" %% "pureconfig" % "0.10.2",
	"org.http4s" %% "http4s-blaze-client" % "0.18.22",
	"org.jsoup" % "jsoup" % "1.11.3",
	"org.scalactic" %% "scalactic" % scalaTestVersion,
	"org.scalatest" %% "scalatest" % scalaTestVersion % "test"
)

libraryDependencies ++= Seq(
	"org.apache.logging.log4j" % "log4j-slf4j-impl",
	"org.apache.logging.log4j" % "log4j-api",
	"org.apache.logging.log4j" % "log4j-core",
).map(_ %  log4jVersion)

libraryDependencies ++= Seq(
	"io.chrisdavenport" %% "log4cats-core",
	"io.chrisdavenport" %% "log4cats-slf4j",
	"io.chrisdavenport" %% "log4cats-log4s"
).map(_ % log4CatVersion withSources() withJavadoc())

libraryDependencies ++= Seq(
	"io.circe" %% "circe-core",
	"io.circe" %% "circe-generic",
	"io.circe" %% "circe-parser"
).map(_ % circeVersion withSources() withJavadoc())

scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint", "-opt:l:inline", "-opt-inline-from:**", "-Ypartial-unification", "-language:higherKinds")

javacOptions ++= Seq("-Xlint")
