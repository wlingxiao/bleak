lazy val commonSettings = Seq(
  organization := "org.goa",
  version := "0.0.1-SNAPSHOT",
  scalaVersion := "2.12.6",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-unchecked"
  )
)

val JacksonVersion = "2.9.4"

lazy val core = Project(id = "goa-core", base = file("core"))
  .configs(IntegrationTest)
  .settings(commonSettings, Defaults.itSettings)
  .settings(libraryDependencies ++= Seq(
    // log
    "org.slf4j" % "slf4j-api" % "1.7.25",
    "ch.qos.logback" % "logback-classic" % "1.2.3" % "runtime",

    // test
    "org.scalatest" %% "scalatest" % "3.0.4" % "it,test",
    "org.mockito" % "mockito-core" % "2.15.0" % Test,
    "org.specs2" %% "specs2-core" % "4.2.0" % Test,
    "junit" % "junit" % "4.12" % Test,
    "com.novocode" % "junit-interface" % "0.11" % Test,
    "com.mashape.unirest" % "unirest-java" % "1.4.9" % IntegrationTest,

    // json support
    "com.fasterxml.jackson.core" % "jackson-core" % JacksonVersion,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % JacksonVersion,

  ))

lazy val goa = Project(id = "goa-project", base = file("."))
  .settings(commonSettings)
  .aggregate(core)