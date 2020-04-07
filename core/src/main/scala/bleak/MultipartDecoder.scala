package bleak

import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues, HttpRequest}
import io.netty.handler.codec.http.multipart.{HttpPostMultipartRequestDecoder, InterfaceHttpData}

import scala.jdk.CollectionConverters._

abstract class MultipartDecoder[T](httpRequest: HttpRequest) {

  private def newDecoder(): HttpPostMultipartRequestDecoder = {
    if (!isMultipleForm) {
      throw new IllegalStateException("")
    }
    new HttpPostMultipartRequestDecoder(httpRequest)
  }

  protected def isMultipleForm: Boolean =
    httpRequest
      .headers()
      .contains(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.MULTIPART_FORM_DATA, true)

  protected def decode(name: String): Option[T] = {
    val decoder = newDecoder()
    try {
      val data = decoder.getBodyHttpData(name)
      if (shouldHandle(data)) {
        Option(handle(data))
      } else None
    } finally {
      decoder.destroy()
    }
  }

  protected def decodeAll(name: String): Iterable[T] = {
    val decoder = newDecoder()
    try {
      decoder.getBodyHttpDatas(name).asScala.collect {
        case data: InterfaceHttpData if shouldHandle(data) => handle(data)
      }
    } finally {
      decoder.destroy()
    }
  }

  protected def decodeAll(): Iterable[T] = {
    val decoder = newDecoder()
    try {
      decoder.getBodyHttpDatas.asScala.collect {
        case data: InterfaceHttpData if shouldHandle(data) => handle(data)
      }
    } finally {
      decoder.destroy()
    }
  }

  protected def shouldHandle(data: InterfaceHttpData): Boolean

  protected def handle(data: InterfaceHttpData): T

}
