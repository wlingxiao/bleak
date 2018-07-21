package goa.annotation

private[goa] trait Routing {

  type route = RouteMapping

  type get = GetRoute

  type post = PostRoute

  type body = RequestBody

  type path = PathParam

  type query = QueryParam

  type header = HeaderParam

  type cookie = CookieParam

}
