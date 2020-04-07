package bleak

import bleak.Content.FileContent
import bleak.RoutingHandler.RouteInfo
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel._
import io.netty.handler.codec.http._
import io.netty.util.ReferenceCountUtil

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Sharable
private class DispatchHandler(app: Application)
    extends SimpleChannelInboundHandler[FullHttpRequest]
    with LazyLogging {

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
      .onComplete {
        case Failure(e) =>
          try {
            log.error("Error occurred", e)
            internalServerError(ctx, e)
          } finally {
            ReferenceCountUtil.release(httpRequest)
          }
        case Success(_) =>
          ReferenceCountUtil.release(httpRequest)
      }
  }

  private def internalServerError(ctx: ChannelHandlerContext, e: Throwable): Unit = {
    val httpResponse = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.INTERNAL_SERVER_ERROR,
      Unpooled.wrappedBuffer(HttpResponseStatus.INTERNAL_SERVER_ERROR.reasonPhrase().getBytes())
    )
    HttpUtil.setTransferEncodingChunked(httpResponse, true)
    ctx.write(httpResponse)
    ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
  }

  private def putRoute(request: Request, routeOpt: Option[Route]): Request = routeOpt match {
    case Some(route) => request.attr(Request.RouteKey, route)
    case None => request
  }

  private def writeResponse(ctx: ChannelHandlerContext, response: Response): Unit =
    response.content match {
      case content: Content.ByteBufContent =>
        writeByteBuf(ctx, response, content.buf)
      case fc: FileContent =>
        writeFile(ctx, response, fc)
      case _ => throw new UnsupportedOperationException()
    }

  private def writeByteBuf(ctx: ChannelHandlerContext, response: Response, buf: ByteBuf): Unit = {
    val httpResponse = new DefaultFullHttpResponse(
      HttpVersion.HTTP_1_1,
      HttpResponseStatus.valueOf(response.status),
      buf)

    val newResponse = response.headers
      .get(HttpHeaderNames.CONTENT_LENGTH)
      .map(_ => response)
      .getOrElse(response.chunked(true))

    encodeHeaders(newResponse, httpResponse.headers())
    val channelF = if (newResponse.chunked) {
      ctx.write(httpResponse)
      ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
    } else {
      ctx.writeAndFlush(httpResponse)
    }
    if (!response.keepAlive) {
      channelF.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def writeFile(ctx: ChannelHandlerContext, response: Response, fc: FileContent): Unit = {
    val httpResponse =
      new DefaultHttpResponse(response.version, HttpResponseStatus.valueOf(response.status))
    val file = fc.file
    val len = file.length()
    val filename = file.getName

    val newResponse = response.headers
      .get(HttpHeaderNames.CONTENT_LENGTH)
      .map(_ => response)
      .getOrElse(response.headers(response.headers.set(HttpHeaderNames.CONTENT_LENGTH, len)))

    encodeHeaders(newResponse, httpResponse.headers())
    ctx.write(httpResponse)

    val fileRegion = new DefaultFileRegion(file, 0, len)
    val channelF = ctx.write(fileRegion, ctx.newProgressivePromise())
    val lastContentF = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
    channelF.addListener(new ChannelProgressiveFutureListener {
      override def operationProgressed(
          future: ChannelProgressiveFuture,
          progress: Long,
          total: Long): Unit = {}

      override def operationComplete(future: ChannelProgressiveFuture): Unit =
        log.trace(s"Transfer complete: $filename")
    })
    if (!response.keepAlive) {
      lastContentF.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def encodeHeaders(res: Response, headers: HttpHeaders): Unit =
    for ((k, v) <- res.headers.iterator) {
      headers.add(k.toString, v.toString)
    }

}
