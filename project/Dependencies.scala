import sbt._

object Dependencies {

  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
  lazy val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion

  lazy val specs2Core = "org.specs2" %% "specs2-core" % specs2CoreVersion

  lazy val nettyHandler = "io.netty" % "netty-handler" % nettyVersion
  lazy val nettyCodecHttp = "io.netty" % "netty-codec-http" % nettyVersion

  lazy val swaggerCore = "io.swagger.core.v3" % "swagger-core" % "2.1.1"
  lazy val swaggerUi = "org.webjars" % "swagger-ui" % "3.25.0"

  lazy val picocli = "info.picocli" % "picocli" % "3.8.2"
  lazy val jsch = "com.jcraft" % "jsch" % "0.1.55"

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

  private val specs2CoreVersion = "4.9.2"
  private val jacksonVersion = "2.10.3"
  private val nettyVersion = "4.1.48.Final"
}
