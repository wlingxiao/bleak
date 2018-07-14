package goa.swagger

import goa.Application
import io.swagger.config.ScannerFactory

import scala.reflect.runtime.universe._

class SwaggerModule(apiConfig: ApiConfig) {

  def init(app: Application): Unit = {
    val routesRules = app.routers.map(x => {
      val s = x.action match {
        case sy: MethodSymbol =>
          x.target.get.getClass.getName + "$." + sy.name.toString
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

}
