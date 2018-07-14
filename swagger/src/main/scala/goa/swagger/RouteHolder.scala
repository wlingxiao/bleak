package goa.swagger

import goa.Route

private[swagger] class RouteHolder(val routes: Map[String, Route]) {

  def get(routeName: String): Route = routes(routeName)

  def exists(routeName: String): Boolean = routes.contains(routeName)

}
