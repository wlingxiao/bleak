package goa

import scala.collection.mutable.ArrayBuffer

trait Controller {

  def get(path: RouteTransformer*)(any: => Any): Unit = {
    addRoute(path, Method.Get, any)
  }

  def post(path: RouteTransformer*)(any: => Any): Unit = {
    addRoute(path, Method.Post, any)
  }

  protected[goa] val middlewareChain: MiddlewareChain = new MiddlewareChain()

  def use(middleware: Middleware): Controller = {
    middlewareChain.use(middleware)
    this
  }

  private[goa] val routers = new ArrayBuffer[Route]()

  protected def addRoute(transformer: Seq[RouteTransformer], method: Method, action: => Any): Unit = {
    val route = Route(transformer, method, this, () => action)
    routers += route
  }

}
