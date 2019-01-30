package bleak
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete

import scala.concurrent.Future

class WebsocketHandler(app: Application, route: WebsocketRoute)
    extends SimpleChannelInboundHandler[HttpRequest] {

  private val responseWriter = new ResponseWriter

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = ???

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit =
    evt match {
      case complete: HandshakeComplete =>
        val request = new HandshakeCompleteRequest(ctx, complete, Some(route))
        val response = Response()
        val executeService = new WebSocketExecuteService
        val context = WebsocketContext(request, response, app = null)

        val future = app.received(context, executeService)
        responseWriter.write(ctx, request, future)
      case _ =>
        super.userEventTriggered(ctx, evt)
    }

}

private class HandshakeCompleteRequest(
    ctx: ChannelHandlerContext,
    complete: HandshakeComplete,
    route: Option[WebsocketRoute])
    extends Request.Proxy { override def request: Request = ??? }

private class WebSocketExecuteService extends Service[Context, Context] {
  override def apply(request: Context): Future[Context] =
    ???
}
