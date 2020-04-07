package bleak.swagger3

import java.time.Instant

import bleak.swagger3.SwaggerRouter.FileInfo
import bleak.util.{DateUtils, IOUtils}
import bleak.{Headers, LazyLogging, Request, Response, Router}
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues}
import io.swagger.v3.core.util.Json

import scala.concurrent.Future
import scala.util.{Try, Using}

class SwaggerRouter(apiDocs: String = "/api-docs") extends Router with LazyLogging {

  get("/swagger-ui.html")(serveIndex _)

  get("/webjars/**")(serveWebJars _)

  private val swaggerResourcePath = "META-INF/resources/webjars/swagger-ui/"

  private val indexHtml = "index.html"

  private val swaggerUIVersionPattern = """.+swagger-ui-(.+)\.jar.+""".r

  private val MediaTypeMap =
    Map(
      "html" -> "text/html",
      "css" -> "text/css",
      "js" -> "application/javascript",
      "png" -> "image/png")

  private val classLoader = Thread.currentThread().getContextClassLoader

  private val srcUrl = classLoader.getResource(swaggerResourcePath)

  private val versionNumber = {
    val swaggerUIVersionPattern(version) = srcUrl.getFile
    version
  }

  log.info(s"Swagger UI version: $versionNumber")

  private val swaggerUIBasePath = swaggerResourcePath + versionNumber + "/"

  get(apiDocs) {
    Future.successful(
      Response(
        headers = Headers(HttpHeaderNames.CONTENT_TYPE -> HttpHeaderValues.APPLICATION_JSON),
        content = Json.mapper().writeValueAsString(Api.openAPI)))
  }

  def serveIndex(request: Request): Future[Response] =
    Future.fromTry(
      loadFileInfo(indexHtml)
        .map(convertIndexHtml)
        .map(buildResponse))

  def serveWebJars(request: Request): Future[Response] = request.paths.splat match {
    case Some(splat) => Future.fromTry(loadFileInfo(splat).map(buildResponse))
    case _ => Future.successful(Response(status = 404))
  }

  def loadFileInfo(filename: String): Try[FileInfo] = Using.Manager { use =>
    val os = use(classLoader.getResourceAsStream(swaggerUIBasePath + filename))
    FileInfo(filename, IOUtils.toBytes(os))
  }

  def convertIndexHtml(fi: FileInfo): FileInfo = {
    val data = new String(fi.data)
      .replaceFirst("https://petstore.swagger.io/v2/swagger.json", apiDocs)
      .replaceAll("""<script src="./""", """<script src="webjars/""")
      .replaceAll("""href="./swagger-ui.css"""", """href="webjars/swagger-ui.css"""")
      .replaceAll("""href="./favicon""", """href="webjars/favicon""")
      .getBytes()

    fi.copy(data = data)
  }

  def buildResponse(fi: FileInfo): Response = {
    val date = DateUtils.formatHttpDate(Instant.now())
    val size = fi.data.length
    Response(
      headers = Headers(
        HttpHeaderNames.CONTENT_TYPE -> detectMediaType(fi),
        HttpHeaderNames.DATE -> date,
        HttpHeaderNames.CONTENT_LENGTH -> size
      ),
      content = fi.data
    )
  }

  private def detectMediaType(fi: FileInfo): CharSequence = {
    val filename = fi.filename
    val idx = filename.lastIndexOf(".")
    MediaTypeMap.getOrElse(filename.substring(idx + 1), HttpHeaderValues.APPLICATION_OCTET_STREAM)
  }

}

object SwaggerRouter {

  case class FileInfo(filename: String, data: Array[Byte])

}
