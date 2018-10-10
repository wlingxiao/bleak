package goa
package swagger2

import goa.json._
import io.swagger.models.Swagger
import goa.util.io

class SwaggerController(app: App) extends Router {

  val BasePath = "META-INF/resources/webjars/swagger-ui/2.2.10-1/"

  get("/swagger-ui/**") { ctx =>
    val request = ctx.request
    request.params.splat match {
      case Some(p) =>
        val fileName = BasePath + p
        val contentType = MimeType(fileName)
        val file = getClass.getClassLoader.getResourceAsStream(fileName)
        val byte = io.toBytes(file)
        ctx.ok()
          .contentType(contentType)
          .contentLength(byte.length)
          .body(Buf(byte))
      case None =>
        val fileName = BasePath + "index.html"
        val file = getClass.getClassLoader.getResourceAsStream(fileName)
        val byte = io.toBytes(file)
        ctx.ok()
          .contentType("text/html")
          .contentLength(byte.length)
          .body(Buf(byte))

    }
  }

  get("/api-docs") { ctx =>
    val swagger = convertToSwagger(app)
    ctx.ok().header("Access-Control-Allow-Origin", "*").json(swagger)
  }


  def convertToSwagger(app: App): Swagger = {
    val swagger = new Swagger
    apis.foreach { api =>
      api.toSwagger(swagger, app)
    }
    swagger
  }

}
