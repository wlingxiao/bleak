package bleak

import java.nio.charset.StandardCharsets

import bleak.Method._
import bleak.Meta._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

trait Router {
  private[this] var _basePath: String = ""
  private[this] var _routes = new ArrayBuffer[Route[_, _]]()

  val consume: Consume = Consume(MimeType.Json)

  val produce: Produce = Produce(MimeType.OctetStream)

  val charset: Charset = Charset(StandardCharsets.UTF_8.displayName())

  def basePath: String = _basePath
  def basePath_=(path: String): Unit = _basePath = path

  def routes: Array[Route[_, _]] = _routes.toArray
  def clearRoutes(): Unit = _routes.clear()
  def addRoute(route: Route[_, _]): this.type = {
    _routes += route
    this
  }

  def findRoute(method: Method, name: Symbol): Option[Route[_, _]] =
    _routes.find { r =>
      r.name == name && r.methods.exists(_ == method)
    }

  def route(
      path: String,
      methods: Iterable[Method] = Seq(Get),
      name: String = "",
      metas: Iterable[Meta] = Nil): HttpRoute = {
    val Metas = buildMetas(metas)
    val routePath = basePath + path
    val routeName = buildRouteName(name, routePath)
    val route =
      HttpRoute(routePath, methods, Symbol(routeName), Metas)
    addRoute(route)
    route
  }

  def get(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute =
    route(path, Seq(Get), name, metas)

  def post(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute =
    route(path, Seq(Post), name, metas)

  def put(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute =
    route(path, Seq(Put), name, metas)

  def delete(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute =
    route(path, Seq(Delete), name, metas)

  def head(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute =
    route(path, Seq(Head), name, metas)

  def options(path: String, name: String = "", metas: Iterable[Meta] = Nil): HttpRoute =
    route(path, Seq(Options), name, metas)

  def ws(path: String, name: String = "", metas: Iterable[Meta] = Nil): WebsocketRoute = {
    val routePath = buildRoutePath(path)
    val routeName = buildRouteName(name, routePath)
    val route = WebsocketRoute(path, Symbol(routeName), buildMetas(metas))
    addRoute(route)
    route
  }

  private def buildRoutePath(path: String): String =
    basePath + path

  private def buildRouteName(name: String, path: String): String = {
    require(name != null, "Name should not be null")
    if (name.isEmpty) s"$path" else name
  }

  private def buildMetas(metas: Iterable[Meta]): Map[Class[_ <: Meta], Meta] = {
    val Metas = mutable.HashMap[Class[_ <: Meta], Meta]()
    for (meta <- metas) {
      Metas.put(meta.getClass, meta)
    }
    if (!Metas.contains(classOf[Consume])) {
      Metas.put(classOf[Consume], consume)
    }
    if (!Metas.contains(classOf[Produce])) {
      Metas.put(classOf[Produce], produce)
    }
    if (!Metas.contains(classOf[Charset])) {
      Metas.put(classOf[Charset], charset)
    }
    Metas.toMap
  }

}
