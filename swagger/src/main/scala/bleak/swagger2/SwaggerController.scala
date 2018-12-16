package bleak
package swagger2

import io.swagger.models.Swagger
import bleak.util.io

class SwaggerController(app: Application) extends Router {

  val BasePath = "META-INF/resources/webjars/swagger-ui/2.2.10-1/"



  def convertToSwagger(app: Application): Swagger = {
    val swagger = new Swagger
    apis.foreach { api =>
      api.toSwagger(swagger, app)
    }
    swagger
  }

}
