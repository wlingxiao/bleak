package bleak
package swagger3

import bleak.util.IOUtils
import io.swagger.v3.core.util.Json

class SwaggerUIRouter(config: Config) extends Router {

  private val swaggerFilePath = "META-INF/resources/webjars/swagger-ui/3.20.2/"

  private val indexFile = swaggerFilePath + "index.html"

  def classLoader(): ClassLoader = {
    Thread.currentThread().getContextClassLoader
  }

  get("/swagger-ui.html") { ctx =>
    val indexHtml = new String(IOUtils.toBytes(classLoader().getResourceAsStream(indexFile)))
      .replaceFirst("https://petstore.swagger.io/v2/swagger.json", "http://127.0.0.1:7865/api-docs")
      .replaceAll("""<script src="./""", """<script src="webjars/""")
      .replaceAll("""href="./swagger-ui.css"""", """href="webjars/swagger-ui.css"""")
      .replaceAll("""href="./favicon""", """href="webjars/favicon""")

    Ok(indexHtml, headers = Map(Fields.ContentType -> MimeType.Html))
  }

  get("/webjars/**") { ctx =>
    ctx.request.paths.splat match {
      case Some(splat) =>
        val res = swaggerFilePath + splat
        val file = classLoader().getResource(res)
        if (file != null) {
          val filename = file.getFile
          val bytes = IOUtils.toBytes(classLoader().getResourceAsStream(res))
          Ok(bytes, headers = Map(Fields.ContentType -> MimeType(filename)))
        } else NotFound()
      case None => NotFound()
    }
  }

  get("/api-docs") { _ =>
    Ok(Json.mapper().writeValueAsString(api.build(config)), headers = Map(Fields.ContentType -> MimeType.Json))
  }
}
