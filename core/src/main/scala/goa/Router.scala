package goa

import java.nio.charset.{Charset, StandardCharsets}

import scala.collection.mutable.ArrayBuffer

trait Router {

  private[goa] val routers = new ArrayBuffer[Route]()

  val mediaType: String = MediaType.Json

  val charset: Charset = StandardCharsets.UTF_8

  val basePath: String = ""

  def route(path: String, methods: Method*): Route = {
    val router = Route(basePath + path, Seq(methods: _*))
      .attr(Symbol("MediaType"), mediaType)
      .attr(Symbol("Charset"), charset)
    routers += router
    router
  }

  def get(path: String): Route = {
    route(path, Method.Get)
  }

  def post(path: String): Route = {
    route(path, Method.Post)
  }

}
