package goa
package netty

import goa.util.Executions
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{cookie, _}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

@Sharable
class DispatchHandler(app: Netty) extends SimpleChannelInboundHandler[FullHttpRequest] {

  protected implicit val ec: ExecutionContext = Executions.directec

  override def channelRead0(chCtx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    val request = createRequest(chCtx, msg)
    val response = createResponse()
    val pipeline = createPipeline()

    pipeline.received(request, response).onComplete {
      case Success(ctx) =>
        writeResponse(ctx, chCtx)
      case Failure(exception) =>
        exception.printStackTrace()
    }
  }

  private def writeResponse(ctx: Context, chCtx: ChannelHandlerContext): Unit = {
    val response = ctx.response
    val responseStatus = HttpResponseStatus.valueOf(response.status.code)
    val responseBody = Option(response.body)
      .map(buf => Unpooled.wrappedBuffer(buf.bytes))
      .getOrElse(Unpooled.EMPTY_BUFFER)
    val fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, responseBody)

    val keepAlive = ctx.request.keepAlive

    if (keepAlive) {
      fullHttpResponse.headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
    }
    for ((k, v) <- response.headers) {
      fullHttpResponse.headers().add(k, v)
    }
    response.cookies.values.map(cookieToNettyCookie).foreach { nc =>
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

  private def createPipeline(): DefaultPipeline = {
    val pipe = DefaultPipeline(app.sessionManager)
    for (m <- app.middlewares) {
      pipe.append(m())
    }
    pipe
  }

  private def createRequest(chCtx: ChannelHandlerContext, msg: FullHttpRequest): NettyRequest = {
    new NettyRequest(msg, chCtx)
  }

  private def createResponse(): Response = {
    NettyResponse()
  }

  private def cookieToNettyCookie(goaCookie: goa.Cookie): cookie.Cookie = {
    val nettyCookie = new cookie.DefaultCookie(goaCookie.name, goaCookie.value.orNull)
    nettyCookie.setDomain(goaCookie.domain.orNull)
    nettyCookie.setPath(goaCookie.path.orNull)
    nettyCookie.setMaxAge(goaCookie.maxAge)
    nettyCookie.setSecure(goaCookie.secure)
    nettyCookie.setHttpOnly(goaCookie.httpOnly)
    nettyCookie
  }
}
