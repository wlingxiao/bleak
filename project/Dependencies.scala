import sbt._

object Dependencies {

  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
  lazy val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion

  lazy val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  lazy val junit = "junit" % "junit" % "4.12"
  lazy val junitInterface = "com.novocode" % "junit-interface" % "0.11"
  lazy val mockitoCore = "org.mockito" % "mockito-core" % "2.15.0"

  lazy val nettyHandler = "io.netty" % "netty-handler" % nettyVersion
  lazy val nettyCodecHttp = "io.netty" % "netty-codec-http" % nettyVersion

  lazy val swaggerCore = "io.swagger.core.v3" % "swagger-core" % "2.1.1"
  lazy val swaggerUi = "org.webjars" % "swagger-ui" % "3.25.0"

  lazy val picocli = "info.picocli" % "picocli" % "3.8.2"
  lazy val jsch = "com.jcraft" % "jsch" % "0.1.55"

  lazy val scalaLogging = "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"

  private val scalatestVersion = "3.1.1"
  private val jacksonVersion = "2.10.3"
  private val nettyVersion = "4.1.29.Final"
}
