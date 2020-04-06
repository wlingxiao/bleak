import Dependencies._

lazy val commonSettings = Seq(
  organization := "com.github.wlingxiao",
  version := "0.0.3-SNAPSHOT",
  scalaVersion := "2.13.1",
  scalacOptions ++= Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-language:implicitConversions",
    "-unchecked"
  )
)

lazy val core = (project in file("core"))
  .withId("bleak-core")
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      scalaLogging,
      nettyHandler,
      nettyCodecHttp,
      logbackClassic % Test,
      specs2Core % Test
    ))

lazy val swagger = (project in file("swagger"))
  .withId("bleak-swagger")
  .settings(commonSettings)
  .settings(
    libraryDependencies ++= Seq(
      swaggerCore,
      jacksonModuleScala,
      swaggerUi,
      logbackClassic % Test,
      specs2Core % Test
    ))
  .dependsOn(core)
