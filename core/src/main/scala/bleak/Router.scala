package bleak


import java.nio.charset.StandardCharsets

import scala.collection.mutable.ArrayBuffer
import Method._
import Meta._

import scala.collection.mutable

trait Router {

  val routes = new ArrayBuffer[Route]()

  val consume: Consume = Consume(MimeType.Json)

  val produce: Produce = Produce(MimeType.OctetStream)

  val charset: Charset = Charset(StandardCharsets.UTF_8.displayName())

  val basePath: String = ""

  def route(path: String, method: Iterable[Method] = Seq(Get), name: String = "", metas: Iterable[Meta] = Nil): HttpRoute = {
    val routeMetas = buildRouteMetas(metas)
    val routePath = basePath + path
    val routeName = buildRouteName(name, routePath)
    val route = HttpRoute(routePath, method, routeName, routeMetas)
    routes += route
    route
  }

  def get(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute = {
    route(path, Seq(Get), name, metas)
  }

  def post(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute = {
    route(path, Seq(Post), name, metas)
  }

  def put(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute = {
    route(path, Seq(Put), name, metas)
  }

  def delete(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute = {
    route(path, Seq(Delete), name, metas)
  }

  def head(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute = {
    route(path, Seq(Head), name, metas)
  }

  def options(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute = {
    route(path, Seq(Options), name, metas)
  }

  def ws(path: String, name: String = "", metas: Iterable[Meta] = Nil): WebSocketRoute = {
    val routePath = buildRoutePath(path)
    val routeName = buildRouteName(name, routePath)
    val route = WebSocketRoute(path, routeName, buildRouteMetas(metas))
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

  private def buildRouteMetas(metas: Iterable[Meta]): Map[Class[_ <: Meta], Meta] = {
    val routeMetas = mutable.HashMap[Class[_ <: Meta], Meta]()
    for (meta <- metas) {
      routeMetas.put(meta.getClass, meta)
    }
    if (!routeMetas.contains(classOf[Consume])) {
      routeMetas.put(classOf[Consume], consume)
    }
    if (!routeMetas.contains(classOf[Produce])) {
      routeMetas.put(classOf[Produce], produce)
    }
    if (!routeMetas.contains(classOf[Charset])) {
      routeMetas.put(classOf[Charset], charset)
    }
    routeMetas.toMap
  }

}
