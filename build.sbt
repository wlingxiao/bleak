lazy val commonSettings = Seq(
  organization := "org.goa",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.4",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-unchecked"
  )
)

lazy val core = Project(id = "goa-core", base = file("core"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    // log
    "org.slf4j" % "slf4j-api" % "1.7.25",
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",

    // test
    "org.scalatest" %% "scalatest" % "3.0.4" % Test,
    "org.mockito" % "mockito-core" % "2.15.0" % Test,

  ))

lazy val goa = Project(id = "goa-project", base = file("."))
  .settings(commonSettings)
  .aggregate(core)