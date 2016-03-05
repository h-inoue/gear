lazy val buildSettings = Seq(
  version := "0.0.0",
  scalaVersion := "2.11.7",
  resolvers += Resolver.bintrayRepo("m1c3", "maven"),
  libraryDependencies ++= Seq(
    "org.scala-lang.modules" % "scala-parser-combinators_2.11" % "1.0.4",
    "org.scala-lang.modules" % "scala-xml_2.11" % "1.0.4",
    "org.scala-lang" % "scala-compiler" % "2.11.7",
    "org.scala-lang" % "scala-library" % "2.11.7",
    "org.scala-lang" % "scala-reflect" % "2.11.7",
    "org.scalatest" % "scalatest_2.11" % "2.2.6",
    "de.tuda.stg" % "rescala_2.11" % "0.0.0")
)

lazy val libDeps = Def.setting { Seq(
  "org.scala-lang.plugins" % "scala-continuations-library_2.11" % "1.0.2"
)}

lazy val exampleDeps = Def.setting { Seq(
  "org.scala-lang" % "scala-swing" % "2.11+"
)}

lazy val root = (project in file(".")).
  settings(buildSettings: _*).
  settings(name := "gear")

lazy val library = (project in file("library")).
  settings(buildSettings: _*).
  settings(
    libraryDependencies ++= libDeps.value,
    autoCompilerPlugins := true,
    scalacOptions += "-P:continuations:enable",
    addCompilerPlugin("org.scala-lang.plugins" % "scala-continuations-plugin_2.11.7" % "1.0.2")
)

lazy val example = (project in file("example")).
  settings(buildSettings: _*).
  settings(
    fork in run := true,
    libraryDependencies ++= exampleDeps.value
  ).dependsOn(library)
