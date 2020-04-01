import Dependencies._

lazy val commonSettings = Seq(
  organization := "com.github.wlingxiao",
  version := "0.0.3-SNAPSHOT",
  scalaVersion := "2.12.7",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-unchecked"
  )
)

lazy val core = Project(id = "bleak-core", base = file("core"))
  .configs(IntegrationTest)
  .settings(commonSettings, Defaults.itSettings)
  .settings(
    libraryDependencies ++= Seq(
      slf4jApi,
      nettyHandler,
      nettyCodecHttp,
      logbackClassic % Test,
      scalatest % "it,test",
      mockitoCore % Test,
      junit % Test,
      junitInterface % Test,
    ))

/*lazy val netty = Project(id = "bleak-netty", base = file("netty"))
  .settings(commonSettings)
  .settings(libraryDependencies ++= Seq(
    nettyHandler,
    nettyCodecHttp,

    logbackClassic % Test,
    scalatest % Test,
    mockitoCore % Test,
  )).dependsOn(core)*/
/*
lazy val swagger = Project(id = "bleak-swagger", base = file("swagger"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      swaggerCore,
      jacksonModuleScala,
      logbackClassic % Test,
      swaggerUi % Test,
      scalatest % Test,
      mockitoCore % Test,
    ))
  .dependsOn(core)

lazy val cli = Project(id = "bleak-cli", base = file("cli"))
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      picocli,
      jsch,
      logbackClassic % Test,
    ))
  .dependsOn(core)*/

parallelExecution in core := false
