package goa

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import goa.channel.nio1.NIO1Server
import goa.channel.{LoggingHandler, Server}
import goa.http1.Http1ServerHandler
import goa.logging.Logging
import goa.marshalling.{DefaultMessageBodyReader, DefaultMessageBodyWriter, MessageBodyReader, MessageBodyWriter}
import goa.matcher.{AntPathMatcher, PathMatcher}

class Goa extends Application {

  val defaultHost: String = "127.0.0.1"

  val defaultPort: Int = 7865

  private[this] var server: Server = _

  val pathMatcher: PathMatcher = new AntPathMatcher()

  private[goa] val mapper: marshalling.ObjectMapper = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.setSerializationInclusion(Include.NON_NULL)
    new marshalling.ObjectMapper(mapper)
  }

  lazy val bodyReader: MessageBodyReader = new DefaultMessageBodyReader(mapper)

  lazy val bodyWriter: MessageBodyWriter = new DefaultMessageBodyWriter(mapper)

  private val routerMiddleware = new RouteMiddleware(bodyWriter, this, pathMatcher)

  var prefix: String = ""

  private def doStart(host: String, port: Int): Unit = {
    use(routerMiddleware)
    server = NIO1Server { ch =>
      ch.pipeline
        .addLast(new LoggingHandler())
        .addLast(new Http1ServerHandler)
        .addLast(new Dispatcher(this))
    }

    server.start(host, port)
  }

  def run(host: String = defaultHost, port: Int = defaultPort): Unit = {
    doStart(host, port)
    server.join()
  }

  def start(host: String = defaultHost, port: Int = defaultPort): Goa = {
    doStart(host, port)
    this
  }

  def join(): Unit = {
    server.join()
  }

  def stop(): Unit = {
    clearRoutes()
    server.stop()
  }

  def mount(controller: Controller): Goa = {
    controller.routers.foreach(addRoute)
    this
  }

  def mount(prefix: String, controller: Controller): Goa = {
    controller.routers.foreach { route =>
      addRoute(Route(this.prefix + prefix + route.path, route.method, route.target, route.action))
    }
    this
  }

}

object Goa extends Logging {

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
