package bleak

import io.netty.handler.codec.http.HttpMethod

import scala.collection.mutable.ArrayBuffer

trait Router {
  private[this] var _basePath: String = ""
  private[this] var _routes = new ArrayBuffer[Route]()

  def basePath: String = _basePath
  def basePath_=(path: String): Unit = _basePath = path

  def routes: Array[Route] = _routes.toArray
  def clearRoutes(): Unit = _routes.clear()
  def addRoute(route: Route): this.type = {
    _routes += route
    this
  }

  def route(path: String, method: HttpMethod = HttpMethod.GET): HttpRoute = {
    val routePath = basePath + path
    val route = HttpRoute(routePath, method)
    addRoute(route)
    route
  }

  def get(path: String): HttpRoute = route(path, HttpMethod.GET)

  def post(path: String): HttpRoute = route(path, HttpMethod.POST)

  def put(path: String): HttpRoute = route(path, HttpMethod.PUT)

  def delete(path: String): HttpRoute = route(path, HttpMethod.DELETE)

  def head(path: String): HttpRoute = route(path, HttpMethod.HEAD)

  def options(path: String): HttpRoute = route(path, HttpMethod.OPTIONS)

  def ws(path: String): WebsocketRoute = {
    val routePath = buildRoutePath(path)
    val route = WebsocketRoute(path)
    addRoute(route)
    route
  }

  private def buildRoutePath(path: String): String =
    basePath + path

}
