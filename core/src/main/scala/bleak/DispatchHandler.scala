package bleak

import bleak.RoutingHandler.RouteInfo
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler
import io.netty.util.ReferenceCountUtil

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Sharable
private class DispatchHandler(app: Application)
    extends SimpleChannelInboundHandler[FullHttpRequest] {

  private implicit val ec: ExecutionContext = Executions.directEc

  override def channelRead0(ctx: ChannelHandlerContext, req: FullHttpRequest): Unit =
    executeHttpRoute(ctx, req)

  private def executeHttpRoute(ctx: ChannelHandlerContext, httpRequest: FullHttpRequest): Unit = {
    val request = Request(httpRequest)
      .attr(Request.LocalAddressKey, ctx.channel().localAddress())
      .attr(Request.RemoteAddressKey, ctx.channel().remoteAddress())
      .attr(Request.ApplicationKey, app)

    val RouteInfo(status, routeOpt) = ctx.channel().attr(RoutingHandler.RouteInfoAttrKey).get()
    new Context.Impl(
      0,
      app.middleware
        .appended(new ActionExecutionMiddleware(status, routeOpt))
        .toIndexedSeq)
      .next(putRoute(request, routeOpt))
      .map(writeResponse(ctx, _))
      .map(_ => ReferenceCountUtil.release(httpRequest))
      .onComplete {
        case Failure(exception) => exception.printStackTrace()
        case Success(value) =>
      }
  }

  private def putRoute(request: Request, routeOpt: Option[Route]): Request = routeOpt match {
    case Some(route) => request.attr(Request.RouteKey, route)
    case None => request
  }

  private def writeResponse(ctx: ChannelHandlerContext, response: Response): Unit = {
    val buf = response.content match {
      case content: Content.ByteBufContent => content.buf
      case content: Content.StringContent => Unpooled.wrappedBuffer(content.text.getBytes())
      case _ => throw new UnsupportedOperationException()
    }

    val httpResponse = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.valueOf(response.status),
      buf)

    val httpHeaders = httpResponse.headers()

    encodeHeaders(response, httpHeaders)
    ctx.write(httpResponse)
    ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
  }

  private def encodeHeaders(res: Response, headers: HttpHeaders): Unit =
    for ((k, v) <- res.headers.iterator) {
      headers.add(k.toString, v.toString)
    }

}
