package goa

import goa.logging.Logging

import scala.collection.mutable.ListBuffer

abstract class Application extends Logging {

  protected[goa] val middlewareChain: MiddlewareChain = new MiddlewareChain()

  private val _routes = ListBuffer[Router]()

  private val _modules = ListBuffer[Module]()

  def routes: Map[String, Router] = ???

  def contextPath: String

  def contextPath_=(contextPath: String): Unit

  @deprecated
  def routers: List[Router] = _routes.toList

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

  def use(module: Module): Application = {
    _modules += module
    this
  }

  /**
    * Init all registered module
    */
  protected def initModules(): Unit = {
    _modules.foreach(_.init(this))
  }

  /**
    * Destroy all registered module
    */
  protected def destroyModules(): Unit = {
    _modules.foreach(_.destroy(this))
  }

  def addRoute(path: String, method: Method, action: => Any): Unit = {
    val route = Router(path, Seq(method))
    addRoute(route)
  }

  def addRoute(route: Router): Unit = {
    log.info(s"Adding route: ${route.methods}     ${route.path}")
    _routes += route
  }

  def clearRoutes(): Unit = {
    _routes.clear()
  }

  def mount(controller: Controller): Application = {
    controller.routers.foreach(addRoute)
    this
  }

}
