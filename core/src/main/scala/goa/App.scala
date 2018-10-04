package goa

import goa.logging.Logging

import scala.collection.mutable.ListBuffer

abstract class App extends Logging {

  @deprecated
  val pipeline: Pipeline = Pipeline()

  private[goa] val middlewares = ListBuffer[Middleware]()

  private val _routes = ListBuffer[Route]()

  private val _modules = ListBuffer[Module]()

  def routes: Map[String, Route] = ???

  def basePath: String = ???

  def basePath(path: String): App = ???

  @deprecated
  def routers: List[Route] = _routes.toList

  def get(path: String)(any: => Any): Unit = {
    addRoute(path, Method.Get, any)
  }

  def post(path: String)(any: => Any): Unit = {
    addRoute(path, Method.Post, any)
  }

  def use(middleware: Middleware): App = {
    middlewares += middleware
    this
  }

  def use(module: Module): App = {
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
    val route = Route(path, Seq(method))
    addRoute(route)
  }

  def addRoute(route: Route): Unit = {
    log.info(s"Adding route: ${route.methods}     ${route.path}")
    _routes += route
  }

  def clearRoutes(): Unit = {
    _routes.clear()
  }

  def mount(controller: Router): App = {
    controller.routers.foreach(addRoute)
    this
  }

  def run(): Unit

  def stop(): Unit

}
