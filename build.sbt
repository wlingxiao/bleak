import Dependencies._

lazy val commonSettings = Seq(
  organization := "com.github.wlingxiao",
  version := "0.0.2-SNAPSHOT",
  scalaVersion := "2.12.7",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding", "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-unchecked"
  )
)

lazy val core = Project(id = "goa-core", base = file("core"))
  .configs(IntegrationTest)
  .settings(commonSettings, Defaults.itSettings)
  .settings(libraryDependencies ++= Seq(
    slf4jApi,
    logbackClassic,

    scalatest % "it,test",
    mockitoCore % Test,
    spec2Core % Test,
    junit % Test,
    junitInterface % Test,
    unirestJava % IntegrationTest,
  ))

lazy val server = Project(id = "goa-server", base = file("server"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
  )).dependsOn(core)

lazy val netty = Project(id = "goa-netty", base = file("netty"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    nettyHandler,
    nettyCodecHttp,

    scalatest % Test,
    mockitoCore % Test,
    fetches % Test,
  )).dependsOn(core)

lazy val swagger = Project(id = "goa-swagger", base = file("swagger"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    swaggerCore,
    jacksonModuleScala,
    swaggerScalaModule exclude("com.fasterxml.jackson.module", "jackson-module-scala"),
    swaggerUi,
    logbackClassic % "runtime",
    scalatest % Test,
    mockitoCore % Test,
  )).dependsOn(core, netty)

parallelExecution in core := false