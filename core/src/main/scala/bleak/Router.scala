package bleak


import java.nio.charset.StandardCharsets

import scala.collection.mutable.ArrayBuffer
import Route.{Attribute, Charset, Consume, Produce}
import Method._

import scala.collection.mutable

trait Router {

  val routes = new ArrayBuffer[Route]()

  val consume: Consume = Consume(MimeType.Json)

  val produce: Produce = Produce(MimeType.Json)

  val charset: Charset = Charset(StandardCharsets.UTF_8.displayName())

  val basePath: String = ""

  def route(path: String, method: Iterable[Method] = Seq(Get), name: String = "", attrs: Iterable[Attribute] = Nil): HttpRoute = {
    val routeAttrs = buildRouteAttrs(attrs)
    val routePath = basePath + path
    val routeName = buildRouteName(name, routePath)
    val route = HttpRoute(routePath, method, routeName, routeAttrs)
    routes += route
    route
  }

  def get(path: String, name: String = "", attrs: Iterable[Attribute] = Nil): HttpRoute = {
    route(path, Seq(Get), name, attrs)
  }

  def post(path: String, name: String = "", attrs: Iterable[Attribute] = Nil): HttpRoute = {
    route(path, Seq(Post), name, attrs)
  }

  def put(path: String, name: String = "", attrs: Iterable[Attribute] = Nil): HttpRoute = {
    route(path, Seq(Put), name, attrs)
  }

  def delete(path: String, name: String = "", attrs: Iterable[Attribute] = Nil): HttpRoute = {
    route(path, Seq(Delete), name, attrs)
  }

  def head(path: String, name: String = "", attrs: Iterable[Attribute] = Nil): HttpRoute = {
    route(path, Seq(Head), name, attrs)
  }

  def options(path: String, name: String = "", attrs: Iterable[Attribute] = Nil): HttpRoute = {
    route(path, Seq(Options), name, attrs)
  }

  def ws(path: String, name: String = "", attrs: Iterable[Attribute] = Nil): WebSocketRoute = {
    val routePath = buildRoutePath(path)
    val routeName = buildRouteName(name, routePath)
    val route = WebSocketRoute(path, routeName, buildRouteAttrs(attrs))
    routes += route
    route
  }

  private def buildRoutePath(path: String): String = {
    basePath + path
  }

  private def buildRouteName(name: String, path: String): String = {
    require(name != null, "Name should not be null")
    if (name.isEmpty) s"$path" else name
  }

  private def buildRouteAttrs(attrs: Iterable[Attribute]): Map[Class[_ <: Attribute], Attribute] = {
    val routeAttrs = mutable.HashMap[Class[_ <: Attribute], Attribute]()
    for (attr <- attrs) {
      routeAttrs.put(attr.getClass, attr)
    }
    if (!routeAttrs.contains(classOf[Consume])) {
      routeAttrs.put(classOf[Consume], consume)
    }
    if (!routeAttrs.contains(classOf[Produce])) {
      routeAttrs.put(classOf[Produce], produce)
    }
    if (!routeAttrs.contains(classOf[Charset])) {
      routeAttrs.put(classOf[Charset], charset)
    }
    routeAttrs.toMap
  }

}
