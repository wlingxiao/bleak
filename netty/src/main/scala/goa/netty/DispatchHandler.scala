package goa
package netty

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{cookie, _}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Sharable
class DispatchHandler(app: App) extends SimpleChannelInboundHandler[FullHttpRequest] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    val keepAlive = HttpUtil.isKeepAlive(msg)
    val request = new NettyRequest(msg, ctx, app.sessionManager)
    val pipeline = Pipeline()
    pipeline.append(app.middlewares: _*)
    pipeline.received(request).onComplete {
      case Success(response) =>
        val responseStatus = HttpResponseStatus.valueOf(response.status.code)
        val responseBody = Option(response.body)
          .map(buf => Unpooled.wrappedBuffer(buf.bytes))
          .getOrElse(Unpooled.EMPTY_BUFFER)
        val fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, responseBody)
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
      case Failure(exception) =>
        exception.printStackTrace()

    }
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
