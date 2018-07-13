package goa

import goa.logging.Loggers

import scala.collection.mutable.ArrayBuffer

trait Controller {

  private val log = Loggers.getLogger(this.getClass)

  def get(path: String)(any: => Any): Unit = {
    addRoute(path, Method.Get, any)
  }

  def post(path: String)(any: => Any): Unit = {
    addRoute(path, Method.Post, any)
  }

  private[goa] val routers = new ArrayBuffer[Route]()

  protected def addRoute(path: String, method: Method, action: => Any): Unit = {
    val route = Route(path, method, this, () => action)
    addRoute(route)
  }

  private def addRoute(route: Route): Controller = {
    assert(route != null, "route")
    log.info(s"Adding route: ${route.method.name}     ${route.path}")
    routers += route
    this
  }

}
