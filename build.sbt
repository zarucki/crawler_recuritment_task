name := "crawler_recruitment_task"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
	"com.github.scopt" %% "scopt" % "3.7.1",
	"com.github.pureconfig" %% "pureconfig" % "0.10.2",
	"org.http4s" %% "http4s-blaze-client" % "0.18.22",
	"org.jsoup" % "jsoup" % "1.11.3"
)

scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint", "-opt:l:inline", "-opt-inline-from:**", "-Ypartial-unification", "-language:higherKinds")

javacOptions ++= Seq("-Xlint")
