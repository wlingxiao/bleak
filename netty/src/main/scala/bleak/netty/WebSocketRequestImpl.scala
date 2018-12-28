package bleak
package netty

import bleak.matcher.PathMatcher
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.HttpHeaders
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete

private class WebSocketRequestImpl(complete: HandshakeComplete,
                                   val ctx: ChannelHandlerContext,
                                   val route: Route,
                                   val pathMatcher: PathMatcher,
                                   val basePath: String) extends AbstractRequest {

  @volatile
  private[this] var _version = Version.Http11

  @volatile
  private[this] var _method = Method.Get

  @volatile
  private[this] var _uri = complete.requestUri()

  override def method: Method = _method

  override def method_=(method: Method): Unit = {
    _method = method
  }

  override def uri: String = {
    _uri
  }

  override def uri_=(uri: String): Unit = {
    _uri = uri
  }

  override def session: Session = ???

  override def session(create: Boolean): Session = ???

  override def version: Version = {
    _version
  }

  override def version_=(version: Version): Unit = {
    _version = version
  }

  override def body: Buf = null

  override def body_=(body: Buf): Unit = ???

  override protected def httpHeaders: HttpHeaders = complete.requestHeaders()
}
