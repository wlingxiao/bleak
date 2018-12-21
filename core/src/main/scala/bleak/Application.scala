package bleak

import bleak.logging.Logging
import bleak.matcher.PathMatcher

import scala.collection.mutable.ListBuffer

trait Application extends Router with Logging {

  private val _modules = ListBuffer[Module]()

  def use(middleware: => Middleware): this.type

  def use(module: Module): Application = {
    _modules += module
    this
  }

  def sessionManager: SessionManager

  def pathMatcher: PathMatcher

  def host: String

  def port: Int

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
    log.info(s"Adding route: ${route.methods}     ${route.path}")
    routes += route
  }

  def clearRoutes(): Unit = {
    routes.clear()
  }

  def use(router: Router): Application = {
    router.routes.foreach(addRoute)
    this
  }

  def run(host: String = this.host, port: Int = this.port): Unit

  def start(host: String = this.host, port: Int = this.port): Unit

  def stop(): Unit
}
