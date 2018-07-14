package goa

import goa.annotation.AnnotationProcessor
import goa.logging.Logging

import scala.collection.mutable.ListBuffer
import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

abstract class Application extends Logging {

  private val processor = new AnnotationProcessor

  protected[goa] val middlewareChain: MiddlewareChain = new MiddlewareChain()

  private val _routes = ListBuffer[Route]()

  def routes: Map[String, Route] = ???

  @deprecated
  def routers: List[Route] = _routes.toList

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

  def addRoute(path: String, method: Method, action: => Any): Unit = {
    val route = Route(path, method, Some(new Controller {}), () => action)
    addRoute(route)
  }

  def addRoute(route: Route): Unit = {
    log.info(s"Adding route: ${route.method.name}     ${route.path}")
    _routes += route
  }

  def clearRoutes(): Unit = {
    _routes.clear()
  }

  def mount[T <: AnyRef : TypeTag : ClassTag](target: T): Application = {
    processor.process[T](target).foreach(addRoute)
    this
  }

}
