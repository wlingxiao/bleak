package goa

case class Route(routeMatchers: Seq[RouteMatcher] = Seq.empty,
                 prefix: String = "",
                 method: Method,
                 controller: Controller,
                 action: () => Any) {

  def path: String = {
    prefix + routeMatchers.filter(_.isInstanceOf[PathRouteMatcher]).map(_.asInstanceOf[PathRouteMatcher].path).mkString
  }
}

object Route {

  def apply(transformers: Seq[RouteTransformer], method: Method, controller: Controller, action: () => Any): Route = {
    val route = Route(action = action, method = method, controller = controller)
    transformers.foldLeft(route) { (route, transformer) => transformer(route) }
  }

  def appendMatcher(matcher: RouteMatcher): RouteTransformer = { route =>
    route.copy(routeMatchers = route.routeMatchers :+ matcher)
  }

}

trait RouteTransformer {
  def apply(route: Route): Route
}

trait RouteMatcher extends RouteTransformer {
  def apply(requestPath: String): Boolean
}

class PathRouteMatcher(val path: String) extends RouteMatcher {
  override def apply(requestPath: String): Boolean = ???

  override def apply(route: Route): Route = Route.appendMatcher(this)(route)
}