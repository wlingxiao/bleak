package bleak.swagger2

import bleak.Router

private[swagger2] class RouteHolder(val routes: Map[String, Router]) {

  def get(routeName: String): Router = routes(routeName)

  def exists(routeName: String): Boolean = routes.contains(routeName)

}
