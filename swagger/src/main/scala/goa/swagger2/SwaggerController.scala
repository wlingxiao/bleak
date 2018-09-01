package goa.swagger2

import java.nio.ByteBuffer

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import goa._
import org.apache.commons.io.IOUtils

class SwaggerController extends Controller {

  get("/swagger-ui/**") { ctx =>
    val BasePath = "META-INF/resources/webjars/swagger-ui/2.2.10-1/"
    val request = ctx.request
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
        val response = ctx.ok()
        response.contentType(format)
        val file = getClass.getClassLoader.getResourceAsStream(fileName)
        val byte = IOUtils.toByteArray(file)
        response.headers.add("Content-Length", byte.length.toString)
        response.body(ByteBuffer.wrap(byte))
      case None =>
        val response = ctx.ok()
        val fileName = BasePath + "index.html"
        response.contentType("text/html")
        val file = getClass.getClassLoader.getResourceAsStream(fileName)
        val byte = IOUtils.toByteArray(file)
        response.headers.add("Content-Length", byte.length.toString)
        response.body(ByteBuffer.wrap(byte))
    }
  }

  private[goa] val mapper: marshalling.ObjectMapper = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.setSerializationInclusion(Include.NON_NULL)
    new marshalling.ObjectMapper(mapper)
  }

  get("/api-docs") { ctx =>
    val swagger = convertToSwagger()
    val body = mapper.writeValueAsByteBuffer(swagger)
    val response = ctx.ok().body(body)
    response.headers.set("Content-Type", "application/json")
    response
  }

}
