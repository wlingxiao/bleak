package goa

import java.nio.charset.{Charset, StandardCharsets}

import scala.collection.mutable.ArrayBuffer

trait Controller {

  private[goa] val routers = new ArrayBuffer[Router]()

  val mediaType: String = MediaType.Json

  val charset: Charset = StandardCharsets.UTF_8

  val basePath: String = ""

  def route(path: String, methods: Method*): Router = {
    val router = Router(basePath + path, Seq(methods: _*))
        .attr(Symbol("MediaType"), mediaType)
        .attr(Symbol("Charset"), charset)
    routers += router
    router
  }

  def get(path: String): Router = {
    route(path, Method.Get)
  }

  def post(path: String): Router = {
    route(path, Method.Post)
  }

}
