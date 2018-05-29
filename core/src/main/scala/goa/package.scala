package object goa {

  def request: Request = Goa.request

  def response: Response = Goa.response

  implicit def string2RouteMatcher(path: String): RouteTransformer = {
    new PathRouteMatcher(path)
  }
}
