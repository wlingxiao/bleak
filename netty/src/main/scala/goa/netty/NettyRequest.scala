package goa
package netty

import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets

import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext

import scala.collection.JavaConverters._
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http._

class NettyRequest(httpRequest: FullHttpRequest, ctx: ChannelHandlerContext, sessionManager: SessionManager) extends Request {

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

  override def params: Param = new Param.QueryParam(this)

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

  override def session(create: Boolean): Session = {
    sessionManager.session(this).getOrElse {
      if (create) {
        sessionManager.createSession(this)
      } else null
    }
  }

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
    val cookies = Option(httpRequest.headers().get(HttpHeaderNames.COOKIE))
      .map(ServerCookieDecoder.STRICT.decode(_).asScala)
      .map(_.map(nettyCookieToCookie).toSet).getOrElse(Set.empty)
    Cookies(cookies)
  }

  override def body: Buf = {
    new NettyBuf(ByteBufUtil.getBytes(httpRequest.content()), HttpUtil.getCharset(httpRequest, StandardCharsets.UTF_8))
  }

  private def nettyCookieToCookie(nettyCookie: cookie.Cookie): goa.Cookie = {
    goa.Cookie(nettyCookie.name(),
      nettyCookie.value(),
      nettyCookie.domain(),
      nettyCookie.path(),
      nettyCookie.maxAge(),
      nettyCookie.isSecure,
      nettyCookie.isHttpOnly)
  }
}
