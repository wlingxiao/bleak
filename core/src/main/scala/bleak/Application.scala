package bleak

import bleak.logging.Logging
import bleak.matcher.PathMatcher

trait Application extends Router with Logging {

  def sessionManager: SessionManager

  def pathMatcher: PathMatcher

  def host: String

  def port: Int

  private def addRoute(route: Route): Unit = {
    log.info(s"Adding route: ${route.methods}     ${route.path}")
    routes += route
  }

  def clearRoutes(): Unit = {
    routes.clear()
  }

  def use(middleware: => Middleware): this.type

  def use(router: Router): this.type = {
    router.routes.foreach(addRoute)
    this
  }

  def run(host: String = this.host, port: Int = this.port): Unit

  def start(host: String = this.host, port: Int = this.port): Unit

  def stop(): Unit
}
