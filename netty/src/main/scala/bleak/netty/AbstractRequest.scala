package bleak
package netty

import java.net.InetSocketAddress

import bleak.matcher.PathMatcher
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.cookie.ServerCookieDecoder
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaders, HttpUtil}

import scala.collection.JavaConverters._

private[netty] trait AbstractRequest extends Request {

  protected def ctx: ChannelHandlerContext

  protected def httpHeaders: HttpHeaders

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
    ???
  }

  override def chunked_=(chunked: Boolean): Unit = ???

  override def headers: Headers = {
    val headers = Headers.empty
    val msgHeaders = httpHeaders.iteratorAsString()
    while (msgHeaders.hasNext) {
      val header = msgHeaders.next()
      headers.add(header.getKey, header.getValue)
    }
    headers
  }

  override def cookies: Cookies = {
    val cookies = httpHeaders.getAll(HttpHeaderNames.COOKIE).asScala.flatMap { str =>
      ServerCookieDecoder.STRICT.decode(str).asScala
    }.map(NettyUtils.nettyCookieToCookie).toSet
    Cookies(cookies)
  }

  override def params: Params = {
    val queryParams = new QueryParams(this)
    Option(route) match {
      case Some(value) =>
        val pathParam = pathMatcher.extractUriTemplateVariables(value.path, path)
        val pattern = pathMatcher.extractPathWithinPattern(value.path, path)
        val splat = if (pattern.nonEmpty) Some(pattern) else None
        new PathParams(queryParams, pathParam.toMap, splat)
      case _ => queryParams
    }
  }

}
