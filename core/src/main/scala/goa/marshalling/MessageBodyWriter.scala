package goa.marshalling

import java.nio.ByteBuffer

import goa.logging.Logging
import goa.{Fields, MediaType, Response}


private[goa] trait MessageBodyWriter {

  def write(response: Response, obj: Any): ByteBuffer

}

private[goa] class DefaultMessageBodyWriter(mapper: ObjectMapper) extends MessageBodyWriter with Logging {
  override def write(response: Response, obj: Any): ByteBuffer = {
    if (obj == null) {
      response.body
    } else {
      obj match {
        case _: Unit => response.body
        case _ if isPrimitive(obj.getClass) =>
          response.headers.add(Fields.ContentType, MediaType.Txt)
          ByteBuffer.wrap(obj.toString.getBytes())
        case _ =>
          response.headers.add(Fields.ContentType, MediaType.Json)
          mapper.writeValueAsByteBuffer(obj)
      }
    }
  }

  private def isPrimitive(clazz: Class[_]): Boolean = {
    clazz.isPrimitive
  }

}