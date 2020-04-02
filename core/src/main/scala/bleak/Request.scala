package bleak

import java.net.{InetSocketAddress, URI}

import bleak.Content.ByteBufContent
import bleak.Params.QueryParams
import bleak.util.AttributeMap
import io.netty.handler.codec.http._

abstract class Request extends Message with AttributeMap {

  /** Get the HTTP version */
  def version: HttpVersion

  def headers: Headers

  def cookies: Cookies

  def content: Content

  /**
    * Returns the HTTP method of this request
    *
    * @return the method name
    */
  def method: HttpMethod

  /**
    * Sets the HTTP method of this request to the give `method`
    *
    * @param method the specified HTTP method
    * @return this request
    */
  def method(method: HttpMethod): Request

  /**
    * Return the uri of this request
    */
  def uri: String

  /**
    * Sets the uri of this request
    */
  def uri(uri: String): Request

  def version(version: HttpVersion): Request

  def content(content: Content): Request

  /** Gets path from uri    */
  def path: String = new URI(uri).getPath

  def params: Params

  /** Remote InetSocketAddress */
  def remoteAddress: InetSocketAddress

  /**
    * Returns the host name of the client or the last proxy that send the request.
    *
    * @return host name of the client
    */
  def remoteHost: String = remoteAddress.getAddress.getHostAddress

  /**
    * Returns the IP source port of the client or the last proxy that send the request
    *
    * @return an integer specifying the port number
    */
  def remotePort: Int = remoteAddress.getPort

  /** Local InetSocketAddress */
  def localAddress: InetSocketAddress

  /** Local host */
  def localHost: String = localAddress.getAddress.getHostAddress

  /**
    * Returns the IP port number of current running server
    *
    * @return an integer specifying the port number
    */
  def localPort: Int = localAddress.getPort

  /** Get User-Agent header */
  def userAgent: Option[String]

  /** Set User-Agent header */
  def userAgent(ua: String): Request

  /**
    * Returns the current [[Route]] associated with this request.
    * If there is no [[Route]], this method returns null
    *
    * @return the [[Route]] associate with this request or null if there is no valid route
    */
  def route: Option[Route]

  def headers(headers: Headers): Request

  def cookies(cookies: Cookies): Request

  override def toString: String =
    s"""Request($method $uri)"""
}

object Request {

  def apply(httpRequest: FullHttpRequest): Request = {
    val uri = httpRequest.uri()
    val method = httpRequest.method()
    val version = httpRequest.protocolVersion()
    val headers = Headers(httpRequest.headers())
    val content = new ByteBufContent(httpRequest.content())

    Impl(uri, method, version, headers, content)
  }

  case class Impl(
      uri: String,
      method: HttpMethod,
      version: HttpVersion,
      headers: Headers,
      content: Content)
      extends Request {

    override def path: String = new URI(uri).getPath

    override def uri(uri: String): Request = copy(uri = uri)

    override def method(method: HttpMethod): Request = copy(method = method)

    override def version(version: HttpVersion): Request = copy(version = version)

    override def headers(headers: Headers): Request = copy(headers = headers)

    override def cookies: Cookies = CookieCodec.decodeRequestCookie(headers)

    override def cookies(cookies: Cookies): Request =
      headers(CookieCodec.encodeRequestCookie(headers, cookies))

    override def content(content: Content): Request = copy(content = content)

    override def params: Params = new QueryParams(uri)

    override def remoteAddress: InetSocketAddress = ???

    override def localAddress: InetSocketAddress = ???

    override def userAgent: Option[String] = None

    override def userAgent(ua: String): Request = ???

    override def route: Option[Route] = ???

  }

}
