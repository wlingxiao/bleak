package bleak

import java.io.RandomAccessFile

import bleak.logging.Logging
import bleak.util.Executions
import io.netty.buffer.{ByteBuf, Unpooled}
import io.netty.channel._
import io.netty.handler.codec.http._
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

private class ResponseWriter extends Logging {

  protected implicit val ec: ExecutionContext = Executions.directec

  def write(ctx: ChannelHandlerContext, request: Request, msg: Future[Context]): Unit =
    msg.onComplete {
      case Success(context) =>
        val response = context.response
        val body = response.body
        body match {
          case file: FileBuf =>
            writeFileResponse(ctx, response, file)
          case _ =>
            val buffer = responseBody(body)
            if (isWebSocket(request)) {
              writeWebSocketResponse(ctx, buffer)
            } else {
              writeResponse(ctx, response, buffer)
            }
        }
      case Failure(e) => ctx.fireExceptionCaught(e)
    }

  private def writeResponse(ctx: ChannelHandlerContext, response: Response, body: ByteBuf): Unit = {
    val fullHttpResponse =
      new DefaultFullHttpResponse(httpVersion(response), responseStatus(response), body)
    val responseHeaders = fullHttpResponse.headers()
    convertHeaders(response, responseHeaders)
    val keepAlive = response.keepAlive
    if (keepAlive) {
      responseHeaders.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
    }
    if (responseHeaders.contains(HttpHeaderNames.CONTENT_LENGTH)) {
      val future = ctx.writeAndFlush(fullHttpResponse)
      if (!keepAlive) {
        future.addListener(ChannelFutureListener.CLOSE)
      }
    } else {
      HttpUtil.setTransferEncodingChunked(fullHttpResponse, true)
      ctx.write(fullHttpResponse)
      val lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
      if (!keepAlive) {
        lastContentFuture.addListener(ChannelFutureListener.CLOSE)
      }
    }
  }

  private def writeWebSocketResponse(ctx: ChannelHandlerContext, body: ByteBuf): Unit =
    ctx.writeAndFlush(new TextWebSocketFrame(body))

  private def writeFileResponse(
      ctx: ChannelHandlerContext,
      response: Response,
      responseBody: FileBuf): Unit = {
    val httpResponse = new DefaultHttpResponse(httpVersion(response), responseStatus(response))
    convertHeaders(response, httpResponse.headers())
    val raf = new RandomAccessFile(responseBody.file, "r")
    val fileName = responseBody.filename
    val fileLen = raf.length()
    HttpUtil.setContentLength(httpResponse, fileLen)
    ctx.write(httpResponse)
    val fileRegion = new DefaultFileRegion(raf.getChannel, 0, fileLen)
    val sendFileFuture = ctx.write(fileRegion, ctx.newProgressivePromise())
    val lastContentFuture = ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT)
    sendFileFuture.addListener(new ChannelProgressiveFutureListener {
      override def operationProgressed(
          future: ChannelProgressiveFuture,
          progress: Long,
          total: Long): Unit =
        log.warn(total.toString)

      override def operationComplete(future: ChannelProgressiveFuture): Unit =
        log.info(s"Transfer complete: $fileName")
    })

    if (!response.keepAlive) {
      lastContentFuture.addListener(ChannelFutureListener.CLOSE)
    }
  }

  private def responseStatus(res: Response): HttpResponseStatus =
    HttpResponseStatus.valueOf(res.status.code)

  private def httpVersion(res: Response): HttpVersion =
    HttpVersion.valueOf(res.version.toString)

  private def convertHeaders(res: Response, headers: HttpHeaders): Unit =
    for ((k, v) <- res.headers) {
      headers.add(k, v)
    }

  private def responseBody(body: Buf): ByteBuf =
    Option(body)
      .map(buf => Unpooled.wrappedBuffer(buf.bytes))
      .getOrElse(Unpooled.EMPTY_BUFFER)

  private def isWebSocket(request: Request): Boolean = {
    val route = request.route
    route != null && route.exists(route => route.isInstanceOf[WebsocketRoute])
  }
}
