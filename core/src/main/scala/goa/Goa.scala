package goa

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import goa.logging.Logging
import goa.marshalling.{DefaultMessageBodyReader, DefaultMessageBodyWriter, MessageBodyReader, MessageBodyWriter}
import goa.matcher.{AntPathMatcher, PathMatcher}

abstract class Goa extends App {
  self: Server =>

  val pathMatcher: PathMatcher = new AntPathMatcher()

  private[goa] val mapper: marshalling.ObjectMapper = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.setSerializationInclusion(Include.NON_NULL)
    new marshalling.ObjectMapper(mapper)
  }

  lazy val bodyReader: MessageBodyReader = new DefaultMessageBodyReader(mapper)

  lazy val bodyWriter: MessageBodyWriter = new DefaultMessageBodyWriter(mapper)

  val routerMiddleware = new RouteMiddleware(bodyWriter, this, pathMatcher)

  private def doStart(): Unit = {
    use(routerMiddleware)
    initModules()
    start()
  }

  def run(): Unit = {
    doStart()
  }

  def stop(): Unit = {
    clearRoutes()
    destroyModules()
    close()
  }
}