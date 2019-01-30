package bleak
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler.HandshakeComplete

class WebsocketHandler(app: Application, route: WebsocketRoute)
    extends SimpleChannelInboundHandler[HttpRequest] {

  private val responseWriter = new ResponseWriter

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = ???

  override def userEventTriggered(ctx: ChannelHandlerContext, evt: Any): Unit =
    evt match {
      case complete: HandshakeComplete =>
        val request = new HandshakeCompleteRequest(ctx, complete, Some(route))
        val executeService = new WebSocketExecuteService
        val future = app.globalMiddleware(request, executeService)
        responseWriter.write(ctx, request, future)
      case _ =>
        super.userEventTriggered(ctx, evt)
    }

}
