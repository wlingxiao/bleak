package bleak

import bleak.matcher.{AntPathMatcher, PathMatcher}

import scala.collection.mutable.ListBuffer

trait Application extends Router with LazyLogging {

  private[this] val middlewareBuffer = ListBuffer[Middleware]()

  private[this] val controllerBuffer = ListBuffer[Router]()

  def pathMatcher: PathMatcher = new AntPathMatcher

  def middleware: List[Middleware] = middlewareBuffer.toList

  def controllers: List[Router] = controllerBuffer.toList

  def use(m: Middleware): this.type = {
    middlewareBuffer.addOne(m)
    this
  }

  def use(router: Router): this.type = {
    router.routes.foreach(addRoute)
    this
  }

}
