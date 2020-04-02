package bleak

import io.netty.handler.codec.http.HttpVersion

abstract class Response extends Message {

  def status: Int

  def status(status: Int): Response

  def version: HttpVersion

  def version(version: HttpVersion): Response

  def headers: Headers

  def headers(headers: Headers): Response

  def content: Content

  def content(content: Content): Response

  def cookies: Cookies

  def cookies(cookies: Cookies): Response

  def keepAlive: Boolean

  def keepAlive(keepAlive: Boolean): Response

  def chunked: Boolean

  def chunked(chunked: Boolean): Response

}

object Response {

  case class Impl(status: Int, version: HttpVersion, headers: Headers, content: Content)
      extends Response {

    override def status(status: Int): Response = copy(status = status)

    override def version(version: HttpVersion): Response = copy(version = version)

    override def content(content: Content): Response = copy(content = content)

    override def headers(headers: Headers): Response = copy(headers = headers)

    override def cookies: Cookies = CookieCodec.decodeResponseCookie(headers)

    override def cookies(cookies: Cookies): Response =
      headers(CookieCodec.encodeResponseCookie(headers, cookies))

    override def keepAlive: Boolean = HttpUtils.isKeepAlive(version, headers)

    override def keepAlive(keepAlive: Boolean): Response =
      headers(HttpUtils.setKeepAlive(version, headers, keepAlive))

    override def chunked: Boolean = HttpUtils.isTransferEncodingChunked(headers)

    override def chunked(chunked: Boolean): Response =
      headers(HttpUtils.setTransferEncodingChunked(headers, chunked))
  }

  def apply(
      status: Int = 200,
      version: HttpVersion = HttpVersion.HTTP_1_1,
      headers: Headers = Headers.empty,
      cookies: Cookies = Cookies.empty,
      content: Content = Content.empty): Response =
    Impl(status, version, CookieCodec.encodeResponseCookie(headers, cookies), content)
      .keepAlive(true)
      .chunked(true)

}
