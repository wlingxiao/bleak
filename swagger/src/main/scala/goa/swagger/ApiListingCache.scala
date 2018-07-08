package goa.swagger

import goa.{Controller, Goa, Method, Route}
import io.swagger.annotations.{Api, ApiOperation}
import io.swagger.config.{ScannerFactory, SwaggerConfig}
import io.swagger.models.Swagger

object ApiListingCache {
  var cache: Option[Swagger] = None

  def listing(docRoot: String, host: String): Option[Swagger] = {
    cache.orElse {
      val scanner = ScannerFactory.getScanner()
      val classes = scanner.classes()
      val reader = new GoaReader(null)
      var swagger = reader.read(classes)

      scanner match {
        case config: SwaggerConfig =>
          swagger = config.configure(swagger)
        case config =>
      }
      cache = Some(swagger)
      cache
    }
    cache.get.setHost(host)
    cache
  }
}

@Api(tags = Array("6666"))
class UserController extends Controller {

  @ApiOperation(value = "/users")
  def getUsers(): Unit = {}

}

object HelloSwagger extends App {
  ScannerFactory.setScanner(new ApiScanner())

  var swaggerConfig = new GoaSwaggerConfig()

  swaggerConfig.description = ""
  swaggerConfig.basePath = ""
  swaggerConfig.contact = ""
  swaggerConfig.version = ""
  swaggerConfig.title = ""
  swaggerConfig.host = ""
  swaggerConfig.termsOfServiceUrl = ""
  swaggerConfig.license = ""
  swaggerConfig.licenseUrl = ""

  val routesRules = new java.util.HashMap[String, Route]()
  routesRules.put("goa.swagger.UserController$.getUsers", Route("/users", Method.Get, new UserController, () => ""))
  val route = new RouteWrapper(routesRules)
  RouteFactory.setRoute(route)

  GoaConfigFactory.setConfig(swaggerConfig)

  val swagger = ApiListingCache.listing("/hh", "127.0.0.1")

  val app = Goa()
  app.get("/api-docs") {
    import goa.response
    response.contentType = "application/json;utf-8"
    response.headers("Access-Control-Allow-Origin") = "*"
    swagger
  }

  app.run()

  println(swagger)
}