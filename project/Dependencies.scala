import sbt._

object Dependencies {

  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % "1.7.25"
  lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.2.3"

  lazy val jacksonCore = "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
  lazy val jacksonModuleScala = "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion

  lazy val scalatest = "org.scalatest" %% "scalatest" % scalatestVersion
  lazy val spec2Core = "org.specs2" %% "specs2-core" % "4.2.0"
  lazy val junit = "junit" % "junit" % "4.12"
  lazy val junitInterface = "com.novocode" % "junit-interface" % "0.11"
  lazy val unirestJava = "com.mashape.unirest" % "unirest-java" % "1.4.9"
  lazy val mockitoCore = "org.mockito" % "mockito-core" % "2.15.0"

  lazy val nettyHandler = "io.netty" % "netty-handler" % nettyVersion
  lazy val nettyCodecHttp = "io.netty" % "netty-codec-http" % nettyVersion

  lazy val swaggerCore = "io.swagger" % "swagger-core" % "1.5.20"
  lazy val swaggerScalaModule = "io.swagger" %% "swagger-scala-module" % "1.0.4"
  lazy val swaggerUi = "org.webjars" % "swagger-ui" % "2.2.10-1"

  lazy val fetches = "com.github.wlingxiao" %% "fetches" % "0.0.1-SNAPSHOT"

  private val scalatestVersion = "3.0.4"
  private val jacksonVersion = "2.9.4"
  private val nettyVersion = "4.1.29.Final"
}
 