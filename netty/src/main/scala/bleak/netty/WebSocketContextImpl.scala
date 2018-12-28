package bleak
package netty

import bleak.matcher.PathMatcher
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete

import scala.concurrent.Future

private[netty] class WebSocketContextImpl(ctx: ChannelHandlerContext,
                                          complete: HandshakeComplete,
                                          pathMatcher: PathMatcher) extends WebSocketContext {

  import AttributeKeys._

  override def send(obj: Any): Unit = {
    obj match {
      case str: String =>
        ctx.channel().writeAndFlush(new TextWebSocketFrame(str))
      case _ =>
    }
  }

  override def on(fun: PartialFunction[WebSocketEvent, Unit]): Unit = {
    ctx.channel().attr(webSocketEventKey).set(fun)
  }

  override def request: Request = {
    val app = ctx.channel().attr(appKey).get()
    new WebSocketRequestImpl(complete, ctx, ctx.channel().attr(webSocketRouteKey).get(), pathMatcher, app.basePath)
  }

  override def response: Response = {
    ctx.channel().attr(responseKey).get()
  }

  override def request_=(req: Request): Unit = {
    ???
  }

  override def response_=(resp: Response): Unit = {
    ctx.channel().attr(responseKey).set(resp)
  }

  override def next(): Future[Context] = ???

  override def session: Option[Session] = ???

  override def app: Application = {
    ctx.channel().attr(appKey).get()
  }
}
