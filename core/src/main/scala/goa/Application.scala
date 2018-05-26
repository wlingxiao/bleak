package goa

import scala.collection.mutable.ArrayBuffer

trait Application {

  def get(path: String)(any: => Any): Unit = {
    route0(path, "GET", any)
  }

  def post(path: String)(any: => Any): Unit = {
    route0(path, "POST", any)
  }

  protected[goa] val middlewareChain: MiddlewareChain = new MiddlewareChain()

  def use(middleware: Middleware): Application = {
    middlewareChain.use(middleware)
    this
  }

  private[goa] val routers = new ArrayBuffer[Router]()

  private def route0(path: String, method: String, any: => Any): Unit = {
    val r = Router(path, method, () => any)
    routers += r
  }

}
