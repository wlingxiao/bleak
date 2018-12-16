package bleak
package netty

import java.net.InetSocketAddress
import java.util

import bleak.matcher.PathMatcher
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues, HttpHeaders, HttpUtil}

import scala.collection.JavaConverters._

private[netty] trait AbstractRequest extends Request {

  protected def ctx: ChannelHandlerContext

  protected def httpHeaders: HttpHeaders

  private val defaultHeaders = new DefaultHeaders(httpHeaders)

  protected def pathMatcher: PathMatcher

  override def remoteAddress: InetSocketAddress = {
    ctx.channel().remoteAddress().asInstanceOf[InetSocketAddress]
  }

  override def localAddress: InetSocketAddress = {
    ctx.channel().localAddress().asInstanceOf[InetSocketAddress]
  }

  override def userAgent: Option[String] = {
    Option(httpHeaders.get(HttpHeaderNames.USER_AGENT))
  }

  override def userAgent_=(ua: String): Unit = {
    httpHeaders.set(HttpHeaderNames.USER_AGENT, ua)
  }

  override def chunked: Boolean = {
    httpHeaders.contains(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true)
  }

  override def chunked_=(chunked: Boolean): Unit = {
    if (chunked) {
      httpHeaders.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
      httpHeaders.remove(HttpHeaderNames.CONTENT_LENGTH)
    } else {
      val encoding = httpHeaders.getAll(HttpHeaderNames.TRANSFER_ENCODING)
      if (!encoding.isEmpty) {
        val values = new util.ArrayList[String](encoding)
        val it = values.iterator
        while (it.hasNext) {
          val value = it.next()
          if (HttpHeaderValues.CHUNKED.contentEqualsIgnoreCase(value)) {
            it.remove()
          }
        }
        if (values.isEmpty) {
          httpHeaders.remove(HttpHeaderNames.TRANSFER_ENCODING)
        } else {
          httpHeaders.set(HttpHeaderNames.TRANSFER_ENCODING, values)
        }
      }
    }
  }

  override def headers: Headers = defaultHeaders

  override def cookies: Cookies = {
    val cookies = httpHeaders.getAll(HttpHeaderNames.COOKIE).asScala.flatMap { str =>
      ServerCookieDecoder.STRICT.decode(str).asScala
    }.map(NettyUtils.nettyCookieToCookie).toSet
    Cookies(cookies)
  }

  override def params: Params[String] = {
    new CombinedParams(this)
  }

  override def query: QueryParams = {
    new DefaultQueryParams(uri)
  }

  override def paths: PathParams = {
    new DefaultPathParams(route.path, path, pathMatcher)
  }

  override def form: FormParams = DefaultFormParams.empty

  override def files: FormFileParams = DefaultFormFileParams.empty
}
