package goa

import java.util.concurrent.ConcurrentHashMap

import goa.logging.Logging

import scala.collection.JavaConverters._

abstract class Application extends Logging {

  protected[goa] val middlewareChain: MiddlewareChain = new MiddlewareChain()

  private val _routes = new ConcurrentHashMap[String, Route]().asScala

  def routes: Map[String, Route] = _routes.toMap

  @deprecated
  def routers: List[Route] = _routes.values.toList

  def get(path: String)(any: => Any): Unit = {
    addRoute(path, Method.Get, any)
  }

  def post(path: String)(any: => Any): Unit = {
    addRoute(path, Method.Post, any)
  }

  def use(middleware: Middleware): Application = {
    middlewareChain.use(middleware)
    this
  }

  def addRoute(path: String, method: Method, action: => Any): Unit = {
    val route = Route(path, method, new Controller {}, () => action)
    addRoute(route)
  }

  def addRoute(route: Route): Unit = {
    log.info(s"Adding route: ${route.method.name}     ${route.path}")
    _routes(route.path) = route
  }

  def clearRoutes(): Unit = {
    _routes.clear()
  }
}
