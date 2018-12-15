package bleak
package netty

import bleak.util.Executions
import io.netty.buffer.Unpooled
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, ChannelOutboundHandlerAdapter, ChannelPromise}
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

private[netty] class ResponseWriter extends ChannelOutboundHandlerAdapter {

  protected implicit val ec: ExecutionContext = Executions.directec

  override def write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise): Unit = {
    msg match {
      case ret: Future[_] =>
        ret onComplete {
          case Success(context) =>
            writeResponse(context.asInstanceOf[Context], ctx)
          case Failure(e) =>
            ctx.fireExceptionCaught(e)
        }
      case _ => super.write(ctx, msg, promise)
    }
  }

  private def writeResponse(ctx: Context, chCtx: ChannelHandlerContext): Unit = {
    val request = ctx.request
    val response = ctx.response
    val responseStatus = HttpResponseStatus.valueOf(response.status.code)
    val responseBody = Option(response.body)
      .map(buf => Unpooled.wrappedBuffer(buf.bytes))
      .getOrElse(Unpooled.EMPTY_BUFFER)
    val fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, responseBody)

    getRoute(ctx) match {
      case Some(value) =>
        value match {
          case ws: WebSocketRoute =>
            chCtx.writeAndFlush(new TextWebSocketFrame(responseBody))
            return
          case http: HttpRoute =>
          case _ =>
        }
      case None =>
    }


    val keepAlive = request.keepAlive

    if (keepAlive) {
      fullHttpResponse.headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
    }
    for ((k, v) <- response.headers) {
      fullHttpResponse.headers().add(k, v)
    }
    response.cookies.values.map(NettyUtils.cookieToNettyCookie).foreach { nc =>
      fullHttpResponse.headers().add(HttpHeaderNames.SET_COOKIE, cookie.ServerCookieEncoder.STRICT.encode(nc))
    }
    if (response.headers.contains(Fields.ContentLength)) {
      val future = chCtx.writeAndFlush(fullHttpResponse)
      if (!keepAlive) {
        future.addListener(ChannelFutureListener.CLOSE)
      }
    } else {
      HttpUtil.setTransferEncodingChunked(fullHttpResponse, true)
      chCtx.write(fullHttpResponse)
      val lastContentFuture = chCtx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
      if (!keepAlive) {
        lastContentFuture.addListener(ChannelFutureListener.CLOSE)
      }
    }
  }

  private def getRoute(ctx: Context): Option[Route] = {
    Option(ctx.request.route)
  }

}
