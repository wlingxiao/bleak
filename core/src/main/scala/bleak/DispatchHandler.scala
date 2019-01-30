package bleak

import bleak.util.Executions
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.util.ReferenceCountUtil

import scala.concurrent.ExecutionContext

@Sharable
private class DispatchHandler(app: Application, status: Status, routeOpt: Option[Route[_, _]])
    extends SimpleChannelInboundHandler[FullHttpRequest] {
  import DispatchHandler.FullRequest
  import RoutingHandler.RouteRequest
  private implicit val ec: ExecutionContext = Executions.directec

  private val responseWriter = new ResponseWriter

  override def channelRead0(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit =
    routeOpt match {
      case Some(route) if route.isInstanceOf[WebsocketRoute] =>
        ctx
          .pipeline()
          .addLast(new WebSocketServerCompressionHandler)
          .addLast(new WebSocketServerProtocolHandler(req.uri(), null, true))
          .addLast(new WebsocketHandler(app, route.asInstanceOf[WebsocketRoute]))

        ReferenceCountUtil.retain(req)
        ctx.fireChannelRead(req)
      case _ => executeHttpRoute(ctx, req)
    }

  private def executeHttpRoute(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit = {
    val request = new FullRequest(new RouteRequest(ctx, Request(req), routeOpt, app), req)
    val res = Response(status = this.status)
    val executeService = new ActionExecutionService(status)
    val future = app.received(HttpContext(request, res, app), executeService)
    responseWriter.write(ctx, request, future)
  }

}

private object DispatchHandler {

  final class FullRequest(val request: Request, req: FullHttpRequest) extends Request.Proxy {
    override def form: FormParams = FormParams(req)
    override def files: FormFileParams = FormFileParams(req)
    override def body: Buf = {
      val content = req.content()
      if (!content.hasArray) {
        val bytes = new Array[Byte](content.readableBytes())
        val readerIndex = content.readerIndex()
        content.getBytes(readerIndex, bytes)
        Buf(bytes)
      } else Buf(content.array())
    }
    override def body_=(body: Buf): Unit =
      req.replace(Unpooled.wrappedBuffer(body.bytes))
  }

}
