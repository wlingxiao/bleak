package bleak
package netty

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.cookie.ClientCookieDecoder

import scala.collection.JavaConverters._

class EmbeddedClient(app: Netty) {

  import EmbeddedClient._

  def get(path: String,
          params: Map[String, String] = Map.empty,
          headers: Map[String, String] = Map.empty,
          cookies: Seq[bleak.Cookie] = Nil): Response = {
    perform(HttpMethod.GET, path, params, headers, cookies)
  }

  def post[T: DataBuilder](path: String,
                           params: Map[String, String] = Map.empty,
                           data: T = (),
                           headers: Map[String, String] = Map.empty,
                           cookies: Seq[bleak.Cookie] = Nil): Response = {
    val body = implicitly[DataBuilder[T]].apply(data)
    perform(HttpMethod.POST, path, params, headers, cookies, body = body)
  }

  def fetch[T: DataBuilder](method: Method,
                            path: String,
                            params: Map[String, String] = Map.empty,
                            data: T = (),
                            headers: Map[String, String] = Map.empty,
                            cookies: Seq[bleak.Cookie] = Nil): Response = {
    val body = implicitly[DataBuilder[T]].apply(data)
    perform(HttpMethod.valueOf(method.name), path, params, headers, cookies, body = body)
  }

  private def perform(method: HttpMethod,
                      path: String,
                      params: Map[String, String],
                      httpHeaders: Map[String, String],
                      httpCookies: Seq[bleak.Cookie],
                      body: ByteBuf = Unpooled.buffer(0)): Response = {
    val queryStringEncoder = new QueryStringEncoder(path)
    for ((k, v) <- params) {
      queryStringEncoder.addParam(k, v)
    }
    val uri = queryStringEncoder.toString
    val fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, body, false)
    for ((k, v) <- httpHeaders) {
      fullHttpRequest.headers().add(k, v)
    }

    if (httpCookies.nonEmpty) {
      val cookieValue = cookie.ClientCookieEncoder.STRICT.encode(httpCookies.map(NettyUtils.cookieToNettyCookie).asJava)
      fullHttpRequest.headers().set(HttpHeaderNames.COOKIE, cookieValue)
    }
    val channel = new EmbeddedChannel(new RouteHandler(app))
    channel.writeInbound(fullHttpRequest)
    val fullHttpResponse = channel.readOutbound[FullHttpResponse]()
    val status = Status(fullHttpResponse.status().code())
    val responseHeaders = fullHttpResponse.headers()
    val content = fullHttpResponse.content()
    if (!content.hasArray) {
      throw new UnsupportedOperationException
    }
    ResponseImpl(status = status, httpHeaders = responseHeaders, body = Buf(content.array()))
  }

}

private object EmbeddedClient {

  trait DataBuilder[-T] {
    def apply(data: T): ByteBuf
  }

  implicit object MapDataBuilder extends DataBuilder[Map[String, AnyVal]] {
    override def apply(data: Map[String, AnyVal]): ByteBuf = {
      val encoder = new QueryStringEncoder("")
      for ((k, v) <- data) {
        encoder.addParam(k, v.toString)
      }
      StringDataBuilder.apply(encoder.toString.stripPrefix("?"))
    }
  }

  implicit object StringDataBuilder extends DataBuilder[String] {
    override def apply(data: String): ByteBuf = {
      if (data == null) AnyValDataBuilder(())
      else Unpooled.wrappedBuffer(data.getBytes())
    }
  }

  implicit object AnyValDataBuilder extends DataBuilder[AnyVal] {
    override def apply(data: AnyVal): ByteBuf = {
      data match {
        case _: Unit => Unpooled.buffer(0)
        case _ => Unpooled.wrappedBuffer(data.toString.getBytes())
      }
    }
  }

}