package goa.swagger

import goa.{Application, Route}
import io.swagger.config.ScannerFactory
import scala.collection.JavaConverters._

class SwaggerModule {

  private val apiScanner = new ApiScanner

  def init(app: Application): Unit = {
    ScannerFactory.setScanner(apiScanner)
    val swaggerConfig = new GoaSwaggerConfig()
    swaggerConfig.description = "swagger test description"
    swaggerConfig.basePath = "/"
    swaggerConfig.contact = "me"
    swaggerConfig.version = "1.0"
    swaggerConfig.title = "swagger test"
    swaggerConfig.host = "127.0.0.1:7865"
    swaggerConfig.termsOfServiceUrl = "http://www.me.com"
    GoaConfigFactory.setConfig(swaggerConfig)
    val routesRules = app.routers.map(x => "exmaple.UserController$.getAllUsers" -> x).toMap.asJava
    val route = new RouteWrapper(routesRules)
    RouteFactory.setRoute(route)
  }

}
