package goa

import goa.channel.Server
import goa.channel.nio1.NIO1Server
import goa.http1.Http1ServerHandler
import goa.matcher.{AntPathMatcher, PathMatcher}

class Goa extends Controller {

  private[this] var server: Server = _

  val pathMatcher: PathMatcher = new AntPathMatcher()

  private val routerMiddleware = new RouterMiddleware(this, pathMatcher)

  var prefix: String = ""

  def run(host: String = "127.0.0.1", port: Int = 7865): Unit = {
    use(routerMiddleware)
    server = NIO1Server { ch =>
      ch.pipeline
        .addLast(new Http1ServerHandler)
        .addLast(new Dispatcher(this))
    }

    server.start(host, port)
    server.join()
  }

  def mount(controller: Controller): Goa = {
    routers ++= controller.routers.map { route =>
      Route(route.path, route.method, route.controller, route.action)
    }
    this
  }

  def mount(prefix: String, controller: Controller): Goa = {
    routers ++= controller.routers.map { route =>
      Route(this.prefix + prefix + route.path, route.method, route.controller, route.action)
    }
    this
  }

}

object Goa {

  private val threadLocal = new ThreadLocal[(Request, Response)]()

  def apply(): Goa = {
    new Goa()
  }

  def request: Request = {
    threadLocal.get()._1
  }

  def response: Response = {
    threadLocal.get()._2
  }

  private[goa] def putMessage(bundle: (Request, Response)): Unit = {
    threadLocal.set(bundle)
  }

  private[goa] def clearMessage(): Unit = {
    threadLocal.remove()
  }
}
