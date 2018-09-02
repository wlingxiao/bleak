package goa
package netty

import java.nio.ByteBuffer

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.{cookie, _}

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

@Sharable
class DispatchHandler(app: App) extends SimpleChannelInboundHandler[FullHttpRequest] {
  override def channelRead0(ctx: ChannelHandlerContext, msg: FullHttpRequest): Unit = {
    val method = Method(msg.method().name())
    val uri = msg.uri()
    val version = Version(msg.protocolVersion().majorVersion(), msg.protocolVersion().minorVersion())
    val headers = Headers.empty
    val msgHeaders = msg.headers().iteratorAsString()
    while (msgHeaders.hasNext) {
      val header = msgHeaders.next()
      headers.add(header.getKey, header.getValue)
    }

    val cookies = Option(msg.headers().get(HttpHeaderNames.COOKIE))
      .map(ServerCookieDecoder.STRICT.decode(_).asScala)
      .map(_.map(nettyCookieToCookie).toSet).getOrElse(Set.empty)

    val keepAlive = HttpUtil.isKeepAlive(msg)
    val body = ByteBuffer.wrap(msg.content().array())
    val request = new Request.Impl(method, uri, version, headers, Cookies(cookies), body)
    app.middlewareChain.messageReceived(request).onComplete {
      case Success(response) =>
        val responseStatus = HttpResponseStatus.valueOf(response.status.code)
        val body = if (response.body != null) {
          Unpooled.wrappedBuffer(response.body)
        } else {
          Unpooled.EMPTY_BUFFER
        }
        val fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, responseStatus, body)
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

  private def nettyCookieToCookie(nettyCookie: cookie.Cookie): goa.Cookie = {
    goa.Cookie(nettyCookie.name(),
      nettyCookie.value(),
      nettyCookie.domain(),
      nettyCookie.path(),
      nettyCookie.maxAge(),
      nettyCookie.isSecure,
      nettyCookie.isHttpOnly)
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
