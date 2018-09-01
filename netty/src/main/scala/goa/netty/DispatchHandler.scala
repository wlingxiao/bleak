package goa
package netty

import java.nio.ByteBuffer

import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelFutureListener, ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._

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
    val keepAlive = HttpUtil.isKeepAlive(msg)
    val body = ByteBuffer.wrap(msg.content().array())
    val request = new Request.Impl(method, uri, version, headers, body)
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
}
