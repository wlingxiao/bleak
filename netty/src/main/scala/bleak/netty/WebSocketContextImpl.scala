package bleak
package netty

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.{BinaryWebSocketFrame, CloseWebSocketFrame, TextWebSocketFrame}

import scala.concurrent.Future

private class WebSocketContextImpl(ctx: ChannelHandlerContext, context: Context) extends WebSocketContext {

  import AttributeKeys._

  override def send(frame: Frame): Unit = {
    frame match {
      case CloseFrame(code, reason) =>
        ctx.channel().writeAndFlush(new CloseWebSocketFrame(code, reason))
      case TextFrame(text) =>
        ctx.channel().writeAndFlush(new TextWebSocketFrame(text))
      case BinaryFrame(bytes) =>
        val buf = Unpooled.wrappedBuffer(bytes)
        ctx.channel().writeAndFlush(new BinaryWebSocketFrame(buf))
      case _ =>
    }
  }

  override def on(fun: PartialFunction[Frame, Unit]): Unit = {
    ctx.channel().attr(WebSocketFrameHandlerKey).set(fun)
  }

  override def request: Request = context.request

  override def response: Response = context.response

  override def request_=(req: Request): Unit = {
    context.request = req
  }

  override def response_=(resp: Response): Unit = {
    context.response = resp
  }

  override def next(): Future[Context] = context.next()

  override def session: Option[Session] = context.session

  override def app: Application = context.app
}
