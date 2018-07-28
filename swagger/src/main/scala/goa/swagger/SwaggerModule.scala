package goa.swagger

import java.lang.reflect.Method

import goa.{Application, Module}
import io.swagger.config.ScannerFactory

class SwaggerModule(apiConfig: ApiConfig) extends Module {

  override def init(app: Application): Unit = {
    app.mount(swaggerController)
    val routesRules = app.routers.map(x => {
      val s = x.action match {
        case sy: Method => x.target.get.getClass.getName + "$." + sy.getName
        case _ => ""
      }
      s -> x
    }).toMap
    val routeHolder = new RouteHolder(routesRules)
    SwaggerFactory.routes = routeHolder
    SwaggerFactory.apiConfig = apiConfig
    val apiScanner = new ApiScanner(apiConfig, routeHolder)
    ScannerFactory.setScanner(apiScanner)
  }

  protected def swaggerController: SwaggerController = {
    new SwaggerController
  }

}
