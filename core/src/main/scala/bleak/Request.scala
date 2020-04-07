package bleak

import java.net.{InetSocketAddress, URI}

import bleak.Content.ByteBufContent
import io.netty.handler.codec.http._
import bleak.Params._

abstract class Request extends Message with Parameter {

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

  def attr[T](key: String): Option[T]

  def attr[T](key: String, obj: T): Request

  def app: Application

  override def toString: String =
    s"""Request($method $uri)"""
}

object Request {

  val RouteKey: String = classOf[Route].getName

  val RemoteAddressKey: String = "bleak.RemoteAddressKey"

  val LocalAddressKey: String = "bleak.LocalAddressKey"

  val ApplicationKey: String = classOf[Application].getName

  def apply(httpRequest: FullHttpRequest): Request = {
    val uri = httpRequest.uri()
    val method = httpRequest.method()
    val version = httpRequest.protocolVersion()
    val headers = Headers(httpRequest.headers())
    val content = new ByteBufContent(httpRequest.content())

    Impl(httpRequest, uri, method, version, headers, content, Map.empty)
  }

  case class Impl(
      httpRequest: FullHttpRequest,
      uri: String,
      method: HttpMethod,
      version: HttpVersion,
      headers: Headers,
      content: Content,
      attribute: Map[String, _])
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

    override def remoteAddress: InetSocketAddress = attr[InetSocketAddress](RemoteAddressKey).orNull

    override def localAddress: InetSocketAddress = attr[InetSocketAddress](LocalAddressKey).orNull

    override def userAgent: Option[String] = headers.get(HttpHeaderNames.USER_AGENT)

    override def userAgent(ua: String): Request =
      headers(headers.set(HttpHeaderNames.USER_AGENT, ua))

    override def route: Option[Route] = attr[Route](RouteKey)

    override def attr[T](key: String): Option[T] =
      attribute.get(key).map(_.asInstanceOf[T])

    override def attr[T](key: String, obj: T): Request =
      copy(attribute = attribute + (key -> obj))

    override def app: Application =
      attr(ApplicationKey)
        .getOrElse(throw new IllegalStateException("App should not be null"))

    override def paths: PathParams = new PathParams(route.map(_.path), path, app.pathMatcher)

    override def args: QueryParams = new QueryParams(uri)

    override def form: FormParams = new FormParams(httpRequest)

    override def files: FormFileParams = new FormFileParams(httpRequest)
  }

}
