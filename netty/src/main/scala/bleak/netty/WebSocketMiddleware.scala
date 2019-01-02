package bleak
package netty

import bleak.matcher.PathMatcher
import bleak.netty.AttributeKeys._
import bleak.util.Executions
import io.netty.channel.{ChannelHandlerContext, ChannelInboundHandlerAdapter}
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete
import io.netty.handler.codec.http.websocketx._
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.util.ReferenceCountUtil

import scala.concurrent.{ExecutionContext, Future}

private class WebSocketHandshakeHandler(context: Context, pathMatcher: PathMatcher) extends ChannelInboundHandlerAdapter {
  protected implicit val ec: ExecutionContext = Executions.directec

  import ActionExecutionMiddleware._

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit = {
    evt match {
      case _: HandshakeComplete =>
        val route = context.request.route.asInstanceOf[WebSocketRoute]
        val wsCtx = new WebSocketContextImpl(ctx, context)
        val ret = route.action(wsCtx) map { ret =>
          convertResult(wsCtx, ret, route)
        }
        ctx.channel().attr(contextKey).set(ret)
      case _ => super.userEventTriggered(ctx, evt)
    }
  }
}

private class WebSocketFrameHandler(path: String, sub: String, allowExtensions: Boolean) extends
  WebSocketServerProtocolHandler(path, sub, allowExtensions) {

  override def decode(ctx: ChannelHandlerContext, wsFrame: WebSocketFrame, out: java.util.List[AnyRef]): Unit = {
    val frameHandler = getFrameHandler(ctx)
    if (frameHandler != null) {
      val frame = convertFrame(wsFrame)
      if (frameHandler.isDefinedAt(frame)) {
        frameHandler(frame)
      }
    }
    super.decode(ctx, wsFrame, out)
  }

  private def convertFrame(frame: WebSocketFrame): Frame = {
    frame match {
      case close: CloseWebSocketFrame => CloseFrame(close.statusCode(), close.reasonText())
      case message: TextWebSocketFrame => TextFrame(message.text())
      case binary: BinaryWebSocketFrame => BinaryFrame(binary.content().array())
      case _ => throw new IllegalStateException(s"Unknown type of websocket frame: ${frame.getClass}")
    }
  }

  private def getFrameHandler(ctx: ChannelHandlerContext): PartialFunction[Frame, Unit] = {
    ctx.channel().attr(WebSocketFrameHandlerKey).get()
  }

}

private class WebSocketMiddleware(channelHandlerContext: ChannelHandlerContext, path: String, request: HttpRequest, pathMatcher: PathMatcher) extends Middleware {
  override def apply(ctx: Context): Future[Context] = {
    channelHandlerContext.pipeline()
      .addLast(new WebSocketServerCompressionHandler())
      .addLast(new WebSocketFrameHandler(path, null, true))
      .addLast(new WebSocketHandshakeHandler(ctx, pathMatcher))
    ReferenceCountUtil.retain(request)
    channelHandlerContext.fireChannelRead(request)
    channelHandlerContext.channel().attr(contextKey).get()
  }
}
