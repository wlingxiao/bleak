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

lazy val core = Project(id = "bleak-core", base = file("core"))
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

lazy val netty = Project(id = "bleak-netty", base = file("netty"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    nettyHandler,
    nettyCodecHttp,

    scalatest % Test,
    mockitoCore % Test,
    fetches % Test,
  )).dependsOn(core)

lazy val swagger = Project(id = "bleak-swagger", base = file("swagger"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    swaggerCore,
    jacksonModuleScala,
    swaggerUi,
    scalatest % Test,
    mockitoCore % Test,
  )).dependsOn(core)

parallelExecution in core := false