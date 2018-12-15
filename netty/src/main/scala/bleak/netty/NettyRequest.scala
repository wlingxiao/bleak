package bleak
package netty

import java.nio.charset.StandardCharsets

import matcher.PathMatcher
import io.netty.buffer.ByteBufUtil
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http._

private[netty] class NettyRequest(httpRequest: HttpRequest,
                                  val ctx: ChannelHandlerContext,
                                  val route: Route,
                                  val pathMatcher: PathMatcher) extends AbstractRequest {


  override protected def httpHeaders: HttpHeaders = httpRequest.headers()

  override def version_=(version: Version): Unit = {
    httpRequest.setProtocolVersion(HttpVersion.valueOf(version.versionString))
  }

  override def method: Method = {
    Method(httpRequest.method().name())
  }

  override def method_=(method: Method): Unit = {
    val httpMethod = HttpMethod.valueOf(method.name)
    httpRequest.setMethod(httpMethod)
  }

  override def uri: String = {
    httpRequest.uri()
  }

  override def uri_=(uri: String): Unit = {
    httpRequest.setUri(uri)
  }

  override def userAgent: Option[String] = {
    Option(httpRequest.headers().get(HttpHeaderNames.USER_AGENT))
  }

  override def userAgent_=(ua: String): Unit = {
    httpRequest.headers().set(HttpHeaderNames.USER_AGENT, ua)
  }

  override def session: Session = {
    session(true)
  }

  override def session(create: Boolean): Session = ???

  override def version: Version = {
    val protocol = httpRequest.protocolVersion()
    Version(protocol.majorVersion(), protocol.minorVersion())
  }

  override def chunked: Boolean = {
    HttpUtil.isTransferEncodingChunked(httpRequest)
  }

  override def chunked_=(chunked: Boolean): Unit = {
    HttpUtil.setTransferEncodingChunked(httpRequest, chunked)
  }

  override def body: Buf = {
    httpRequest match {
      case full: FullHttpRequest =>
        new NettyBuf(ByteBufUtil.getBytes(full.content()), HttpUtil.getCharset(httpRequest, StandardCharsets.UTF_8))
      case _ => null
    }
  }

  override def body_=(body: Buf): Unit = {
    ???
  }
}
