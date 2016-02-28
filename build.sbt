name := "gear"

version := "0.1"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  "org.scala-lang.plugins" % "scala-continuations-library_2.11" % "1.0.2",
  "org.scalatest" % "scalatest_2.11" % "2.2.6",
  "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4"
)

autoCompilerPlugins := true

scalacOptions += "-P:continuations:enable"

addCompilerPlugin("org.scala-lang.plugins" % "scala-continuations-plugin_2.11.7" % "1.0.2")
