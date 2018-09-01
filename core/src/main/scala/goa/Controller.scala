package goa

import scala.collection.mutable.ArrayBuffer

trait Controller {

  private[goa] val routers = new ArrayBuffer[Router]()

  def route(path: String, methods: Method*): Router = {
    val router = Router(path, Seq(methods: _*))
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
