name := "crawler_recruitment_task"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies ++= Seq(
	"com.github.scopt" %% "scopt" % "3.7.1",
	"com.github.pureconfig" %% "pureconfig" % "0.10.2"
)

scalacOptions ++= Seq("-deprecation", "-feature", "-Xlint", "-opt:l:inline", "-opt-inline-from:**")

javacOptions ++= Seq("-Xlint")
