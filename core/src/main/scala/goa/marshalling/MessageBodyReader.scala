package goa.marshalling

import java.nio.ByteBuffer

import goa.{Fields, MediaType, Method, Request}

import scala.reflect.ClassTag

private[goa] trait MessageBodyReader {

  def parse[T: ClassTag](request: Request): Option[T]

}

private[goa] class DefaultMessageBodyReader(mapper: ObjectMapper) extends MessageBodyReader {
  override def parse[T: ClassTag](request: Request): Option[T] = {
    if (hasBody(request) && isJson(request)) {
      Option(mapper.parse[T](request.body))
    } else None
  }

  private def isJson(request: Request): Boolean = {
    request.contentType.isDefined && request.contentType.get.toLowerCase.startsWith(MediaType.Json)
  }

  private def hasBody(request: Request): Boolean = {
    request.method == Method.Post || request.method == Method.Put
  }

}

