package bleak


import java.nio.charset.StandardCharsets

import scala.collection.mutable.ArrayBuffer
import Route.{Attribute, Charset, Consume, Produce}
import Method._

import scala.collection.mutable

trait Router {

  val routes = new ArrayBuffer[Route]()

  val consume: Consume = Consume(MediaType.Json)

  val produce: Produce = Produce(MediaType.Json)

  val charset: Charset = Charset(StandardCharsets.UTF_8.displayName())

  val basePath: String = ""

  private def route(path: String, method: Method, name: String, attrs: Attribute*): HttpRoute = {
    val routePath = basePath + path
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
    val route = HttpRoute(routePath, method, name, routeAttrs.toMap)
    routes += route
    route
  }

  def get(path: String, attrs: Attribute*): HttpRoute = {
    route(path, Get, s"GET $path")
  }

  def get(path: String, name: String, attrs: Attribute*): HttpRoute = {
    route(path, Get, name)
  }

  def post(path: String, attrs: Attribute*): HttpRoute = {
    route(path, Post, s"GET $path")
  }

  def post(path: String, name: String, attrs: Attribute*): HttpRoute = {
    route(path, Post, name)
  }

  def ws(path: String): WebSocketRoute = {
    val route = WebSocketRoute(path, s"WS $path", Map.empty)
    routes += route
    route
  }

}
