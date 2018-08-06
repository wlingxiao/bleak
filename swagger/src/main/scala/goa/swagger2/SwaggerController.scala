package goa.swagger2

import java.nio.ByteBuffer

import goa._
import org.apache.commons.io.IOUtils

class SwaggerController extends Controller {

  get("/swagger-ui/**") {
    val BasePath = "META-INF/resources/webjars/swagger-ui/2.2.10-1/"
    request.params.get("splat") match {
      case Some(p) =>
        val fileName = BasePath + p
        val format = if (fileName.endsWith("html")) {
          "text/html"
        } else if (fileName.endsWith("css")) {
          "text/css"
        } else if (fileName.endsWith("js")) {
          "text/javascript"
        } else if (fileName.endsWith("png")) {
          "img/png"
        } else {
          "text/plain"
        }
        response.contentType = format
        val file = getClass.getClassLoader.getResourceAsStream(fileName)
        val byte = IOUtils.toByteArray(file)
        response.headers.add("Content-Length", byte.length.toString)
        response.body = ByteBuffer.wrap(byte)
      case None =>
        val fileName = BasePath + "index.html"
        response.contentType("text/html")
        val file = getClass.getClassLoader.getResourceAsStream(fileName)
        val byte = IOUtils.toByteArray(file)
        response.headers.add("Content-Length", byte.length.toString)
        response.body = ByteBuffer.wrap(byte)
    }
    ()
  }

  get("/api-docs") {
    response.chunked = true
    response.contentType = "application/json;utf-8"
    SwaggerFactory.swagger
  }

}
