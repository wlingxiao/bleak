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

  private val _modules = ListBuffer[Module]()

  def routes: Map[String, Route] = ???

  def contextPath: String

  def contextPath_=(contextPath: String): Unit

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

  def mount[T <: AnyRef : ClassTag](target: T): Application = {
    processor.process[T](target).foreach(addRoute)
    this
  }

  def mount(controller: Controller): Application = {
    controller.routers.foreach(addRoute)
    this
  }

}
