package bleak

import io.netty.handler.codec.http.{
  DefaultFullHttpResponse,
  FullHttpResponse,
  HttpResponseStatus,
  HttpUtil,
  HttpVersion
}

abstract class Response extends Message {

  def status: Int

  def status(status: Int): Response

}

object Response {

  class Impl(var httpResponse: FullHttpResponse, var extraContent: Option[Content])
      extends Response {

    override def status(status: Int): Response = {
      httpResponse.setStatus(HttpResponseStatus.valueOf(status))
      this
    }

    override def version(version: HttpVersion): this.type = {
      httpResponse.setProtocolVersion(version)
      this
    }

    override def status: Int = httpResponse.status().code()

    override def version: HttpVersion = httpResponse.protocolVersion()

    override def headers: Headers = Headers(httpResponse.headers())

    override def content: Content =
      extraContent.getOrElse(Content(httpResponse.content()))

    override def content(content: Content): this.type = {
      content match {
        case content: Content.ByteBufContent =>
          httpResponse = httpResponse.replace(content.buf)
        case content: Content.FileContent =>
          extraContent = Option(content)
        case _ => throw new UnsupportedOperationException
      }
      this
    }

    override def keepAlive: Boolean = HttpUtil.isKeepAlive(httpResponse)

    override def keepAlive(keepAlive: Boolean): this.type = {
      HttpUtil.setKeepAlive(httpResponse, keepAlive)
      this
    }

    override def chunked: Boolean = HttpUtil.isTransferEncodingChunked(httpResponse)

    override def chunked(chunked: Boolean): this.type = {
      HttpUtil.setTransferEncodingChunked(httpResponse, chunked)
      this
    }

    override def headers(headers: Headers): this.type = {
      headers match {
        case impl: Headers.Impl => httpResponse.headers().set(impl.httpHeaders)
        case _ => throw new IllegalStateException()
      }
      this
    }

    override def cookies: Cookies = CookieCodec.decodeResponseCookie(headers)

    override def cookies(cookies: Cookies): this.type =
      headers(CookieCodec.encodeResponseCookie(headers, cookies))

  }

  def apply(
      status: Int = 200,
      version: HttpVersion = HttpVersion.HTTP_1_1,
      headers: Headers = Headers.empty,
      cookies: Cookies = Cookies.empty,
      content: Content = Content.empty): Response = {
    var httpResponse: FullHttpResponse =
      new DefaultFullHttpResponse(version, HttpResponseStatus.valueOf(status))
    var extraContent: Option[Content] = None
    httpResponse
      .headers()
      .set(
        CookieCodec.encodeResponseCookie(headers, cookies).asInstanceOf[Headers.Impl].httpHeaders)
    content match {
      case content: Content.ByteBufContent =>
        httpResponse = httpResponse.replace(content.buf)
      case content: Content.FileContent =>
        extraContent = Option(content)
      case _ =>
    }
    new Impl(httpResponse, extraContent)
  }

}
