package bleak

import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http._
import scala.jdk.CollectionConverters._

class EmbeddedClient(app: Application) {

  def get(
      path: String,
      params: Map[String, String] = Map.empty,
      headers: Map[String, String] = Map.empty,
      cookies: Seq[bleak.Cookie] = Nil): Response =
    perform(HttpMethod.GET, path, params, headers, cookies, content = Content.empty)

  def post(
      path: String,
      params: Map[String, String] = Map.empty,
      content: Content = Content.empty,
      headers: Map[String, String] = Map.empty,
      cookies: Seq[bleak.Cookie] = Nil): Response =
    perform(HttpMethod.POST, path, params, headers, cookies, content = content)

  def fetch(
      method: HttpMethod,
      path: String,
      params: Map[String, String] = Map.empty,
      content: Content = Content.empty,
      headers: Map[String, String] = Map.empty,
      cookies: Seq[bleak.Cookie] = Nil): Response =
    perform(method, path, params, headers, cookies, content)

  private def perform(
      method: HttpMethod,
      path: String,
      params: Map[String, String],
      headers: Iterable[(String, String)],
      cookies: Seq[bleak.Cookie],
      content: Content): Response = {
    val queryStringEncoder = new QueryStringEncoder(path)
    for ((k, v) <- params) {
      queryStringEncoder.addParam(k, v)
    }
    val uri = queryStringEncoder.toString
    val fullHttpRequest =
      new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, method, uri, toByteBuf(content), false)
    for ((k, v) <- headers) {
      fullHttpRequest.headers().add(k, v)
    }

    if (cookies.nonEmpty) {
      val cookieValue = cookie.ClientCookieEncoder.STRICT
        .encode(cookies.map(CookieCodec.cookieToNettyCookie).asJava)
      fullHttpRequest.headers().set(HttpHeaderNames.COOKIE, cookieValue)
    }
    val channel = new EmbeddedChannel(new RoutingHandler(app))
    channel.writeInbound(fullHttpRequest)
    val fullHttpResponse = channel.readOutbound[FullHttpResponse]()
    val status = fullHttpResponse.status().code()
    val responseHeaders = fullHttpResponse.headers()
    val responseContent = fullHttpResponse.content()
    if (!responseContent.hasArray) {
      throw new UnsupportedOperationException
    }

    Response(
      status = status,
      headers = Headers(responseHeaders),
      content = new Content.ByteBufContent(responseContent))
  }

  def toByteBuf(content: Content): ByteBuf = content match {
    case content: Content.ByteBufContent => content.buf
    case content: Content.StringContent => Unpooled.wrappedBuffer(content.text.getBytes())
    case _ => throw new UnsupportedOperationException()
  }

}
