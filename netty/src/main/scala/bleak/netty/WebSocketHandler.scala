package bleak
package netty

import AttributeKeys._
import bleak.matcher.PathMatcher
import util.Executions
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.{FullHttpRequest, HttpRequest}
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.handler.codec.http.websocketx.{CloseWebSocketFrame, TextWebSocketFrame, WebSocketFrame, WebSocketServerProtocolHandler}
import io.netty.util.ReferenceCountUtil

import scala.concurrent.{ExecutionContext, Future}

private[netty] class WebSocketHandshakeHandler(pathMatcher: PathMatcher) extends ChannelInboundHandlerAdapter {
  protected implicit val ec: ExecutionContext = Executions.directec

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
    evt match {
      case complete: HandshakeComplete =>
        val route = getWebSocketRoute(ctx)
        val response = getResponse(ctx)
        val wsCtx = new WebSocketContextImpl(ctx, complete, pathMatcher)
        val ret = route.action(wsCtx) map { ret =>
          response.status = ret.status
          for ((k, v) <- ret.headers) {
            response.headers.add(k, v)
          }
          for (c <- ret.cookies) {
            response.cookies.add(c)
          }
          wsCtx.response = response
          wsCtx.response.body = ret.body
          wsCtx
        }
        ctx.channel().attr(contextKey).set(ret)
      case _ => super.userEventTriggered(ctx, evt)
    }
  }

  private def getWebSocketRoute(ctx: ChannelHandlerContext): WebSocketRoute = {
    ctx.channel().attr(webSocketRouteKey).get()
  }

  private def getResponse(ctx: ChannelHandlerContext): Response = {
    ctx.channel().attr(responseKey).get()
  }

}

private[netty] class WebSocketHandler(path: String, sub: String, allowExtensions: Boolean) extends
  WebSocketServerProtocolHandler(path, sub, allowExtensions) {

  override def decode(ctx: ChannelHandlerContext, frame: WebSocketFrame, out: java.util.List[AnyRef]): Unit = {
    frame match {
      case _: CloseWebSocketFrame =>
        ctx.channel().attr(webSocketEventKey).get()(new Close)
      case message: TextWebSocketFrame =>
        ctx.channel().attr(webSocketEventKey).get()(Text(message.text()))
      case _ =>
    }
    super.decode(ctx, frame, out)
  }

}

private[netty] class WebSocketMiddleware(channelHandlerContext: ChannelHandlerContext, path: String, request: HttpRequest, pathMatcher: PathMatcher) extends Middleware {
  override def apply(ctx: Context): Future[Context] = {
    channelHandlerContext.pipeline()
      .addLast(new WebSocketServerCompressionHandler())
      .addLast(new WebSocketHandler(path, null, true))
      .addLast(new WebSocketHandshakeHandler(pathMatcher))
    ReferenceCountUtil.retain(request)
    channelHandlerContext.fireChannelRead(request)
    channelHandlerContext.channel().attr(contextKey).get()
  }
}
