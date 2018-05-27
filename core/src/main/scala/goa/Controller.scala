package goa

import scala.collection.mutable.ArrayBuffer

trait Controller {

  def get(path: String)(any: => Any): Unit = {
    addRoute(path, "GET", any)
  }

  def post(path: String)(any: => Any): Unit = {
    addRoute(path, "POST", any)
  }

  protected[goa] val middlewareChain: MiddlewareChain = new MiddlewareChain()

  def use(middleware: Middleware): Controller = {
    middlewareChain.use(middleware)
    this
  }

  private[goa] val routers = new ArrayBuffer[Router]()

  protected def addRoute(path: String, method: String, any: => Any): Unit = {
    val r = Router(path, method, this, () => any)
    routers += r
  }

}
