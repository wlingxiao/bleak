package goa

import goa.logging.Logging

import scala.collection.mutable.ListBuffer

abstract class App extends Router with Logging {

  @deprecated
  val pipeline: Pipeline = Pipeline()

  private[goa] val middlewares = ListBuffer[Middleware]()

  private val _modules = ListBuffer[Module]()

  def use(middleware: Middleware): App = {
    middlewares += middleware
    this
  }

  def use(module: Module): App = {
    _modules += module
    this
  }

  def sessionManager: SessionManager

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

  def addRoute(route: Route): Unit = {
    log.info(s"Adding route: ${route.method}     ${route.path}")
    routes += route
  }

  def clearRoutes(): Unit = {
    routes.clear()
  }

  def mount(router: Router): App = {
    router.routes.foreach(addRoute)
    this
  }

  def run(): Unit

  def stop(): Unit

}
