package bleak
package netty

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext

import scala.collection.JavaConverters._
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http._

class NettyRequest(httpRequest: FullHttpRequest, ctx: ChannelHandlerContext) extends Request {

  override def method: Method = {
    Method(httpRequest.method().name())
  }

  override def method(method: Method): Request = {
    val httpMethod = HttpMethod.valueOf(method.name)
    httpRequest.setMethod(httpMethod)
    this
  }

  override def uri: String = {
    httpRequest.uri()
  }

  override def uri(uri: String): Request = {
    httpRequest.setUri(uri)
    this
  }

  override def params: Params = new Params.QueryParams(this)

  override def remoteAddress: InetSocketAddress = {
    ctx.channel().remoteAddress().asInstanceOf[InetSocketAddress]
  }

  override def localAddress: InetSocketAddress = {
    ctx.channel().localAddress().asInstanceOf[InetSocketAddress]
  }

  override def route: Route = ???

  override def session: Session = {
    session(true)
  }

  override def session(create: Boolean): Session = ???

  override def version: Version = {
    val protocol = httpRequest.protocolVersion()
    Version(protocol.majorVersion(), protocol.minorVersion())
  }

  override def headers: Headers = {
    val headers = Headers.empty
    val msgHeaders = httpRequest.headers().iteratorAsString()
    while (msgHeaders.hasNext) {
      val header = msgHeaders.next()
      headers.add(header.getKey, header.getValue)
    }
    headers
  }

  override def cookies: Cookies = {
    val cookies = httpRequest.headers().getAll(HttpHeaderNames.COOKIE).asScala.flatMap { str =>
      ServerCookieDecoder.STRICT.decode(str).asScala
    }.map(NettyUtils.nettyCookieToCookie).toSet
    Cookies(cookies)
  }

  override def body: Buf = {
    new NettyBuf(ByteBufUtil.getBytes(httpRequest.content()), HttpUtil.getCharset(httpRequest, StandardCharsets.UTF_8))
  }
}
