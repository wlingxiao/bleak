package bleak.swagger3

import java.nio.file.attribute.FileTime
import java.time.Instant

import bleak.swagger3.SwaggerUIRouter.FileInfo
import bleak.util.{DateUtils, IOUtils}
import bleak.{Headers, Request, Response, Router}
import io.netty.handler.codec.http.HttpHeaderNames
import io.swagger.v3.core.util.Json

import scala.concurrent.Future
import scala.util.{Try, Using}

class SwaggerUIRouter(apiDocs: String = "/api-docs") extends Router {

  get("/swagger-ui.html")(serveIndex _)

  get("/webjars/**")(serveWebJars _)

  private val swaggerFilePath = "META-INF/resources/webjars/swagger-ui/"

  private val indexHtml = "index.html"

  private def httpCacheSeconds = Int.MaxValue

  private val swaggerUIVersionPattern = """.+swagger-ui-(.+)\.jar.+""".r

  def classLoader: ClassLoader =
    Thread.currentThread().getContextClassLoader

  private val srcUrl = classLoader.getResource(swaggerFilePath)

  private val versionNumber = {
    val swaggerUIVersionPattern(version) = srcUrl.getFile
    version
  }

  get(apiDocs) {
    Future.successful(
      Response(
        headers = Headers(HttpHeaderNames.CONTENT_TYPE -> "application/json"),
        content = Json.mapper().writeValueAsString(Api.openAPI)))
  }

  val swaggerBaseDir = swaggerFilePath + versionNumber + "/"

  private val MediaTypeMap =
    Map("html" -> "text/html", "css" -> "text/css", "js" -> "application/javascript")

  def serveIndex(request: Request): Future[Response] =
    Future.fromTry(
      loadFileInfo(indexHtml)
        .map(convertIndexHtml)
        .map(buildResult))

  def serveWebJars(request: Request): Future[Response] =
    request.params.splat match {
      case Some(splat) =>
        Future.fromTry(loadFileInfo(splat).map(buildResult))
      case _ =>
        import bleak.Executions.global
        Future(Response(status = 404))
    }

  def loadFileInfo(filename: String): Try[FileInfo] = Using.Manager { use =>
    val os = use(classLoader.getResourceAsStream(swaggerBaseDir + filename))
    FileInfo(filename, null, IOUtils.toBytes(os))
  }

  def convertIndexHtml(fileInfo: FileInfo): FileInfo = {
    val data = new String(fileInfo.data)
      .replaceFirst("https://petstore.swagger.io/v2/swagger.json", apiDocs)
      .replaceAll("""<script src="./""", """<script src="webjars/""")
      .replaceAll("""href="./swagger-ui.css"""", """href="webjars/swagger-ui.css"""")
      .replaceAll("""href="./favicon""", """href="webjars/favicon""")
      .getBytes()

    fileInfo.copy(data = data)
  }

  def buildResult(target: FileInfo): Response = {
    val now = Instant.now()
    val date = DateUtils.formatHttpDate(Instant.now())
    val expires = DateUtils.formatHttpDate(now.plusSeconds(httpCacheSeconds))
//    val lastModified = DateUtils.formatHttpDate(target.lastModified.toInstant)
    val cacheControl = "private, max-age=" + httpCacheSeconds
    val size = target.data.length
    Response(
      headers = Headers(
        HttpHeaderNames.CONTENT_TYPE -> detectMediaType(target),
        HttpHeaderNames.DATE -> date,
      ),
      content = target.data)
  }

  private def detectMediaType(target: FileInfo): String = {
    val filename = target.filename
    val idx = filename.lastIndexOf(".")
    MediaTypeMap.get(filename.substring(idx + 1)) match {
      case Some(value) => value
      case None =>
        println(filename)
        "application/octet-stream"
    }
  }

}

object SwaggerUIRouter {

  case class FileInfo(filename: String, lastModified: FileTime, data: Array[Byte])

}
