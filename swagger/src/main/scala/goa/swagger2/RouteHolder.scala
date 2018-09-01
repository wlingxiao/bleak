package goa.swagger2

import goa.Router

private[swagger2] class RouteHolder(val routes: Map[String, Router]) {

  def get(routeName: String): Router = routes(routeName)

  def exists(routeName: String): Boolean = routes.contains(routeName)

}
