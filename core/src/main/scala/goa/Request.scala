package goa

import java.net.{InetAddress, InetSocketAddress, URI}
import java.nio.ByteBuffer

import goa.marshalling.MessageBodyReader
import goa.matcher.PathMatcher

abstract class Request extends Message {

  /**
    * Returns the HTTP method of this request
    *
    * @return the method name
    */
  def method: Method

  /**
    * Sets the HTTP method of this request to the give `method`
    *
    * @param method the specified HTTP method
    * @return this request
    */
  def method(method: Method): Request

  /**
    * Return the uri of this request
    */
  def uri: String

  /**
    * Sets the uri of this request
    */
  def uri(uri: String): Request

  /** Get path from uri    */
  def path: String = new URI(uri).getPath

  def params: Param

  /**
    * The InetSocketAddress of the client
    */
  def remoteSocketAddress: InetSocketAddress

  /** Remote InetAddress */
  def remoteAddress: InetAddress = remoteSocketAddress.getAddress

  /** Remote host */
  def remoteHost: String = remoteAddress.getHostAddress

  /** Remote port */
  def remotePort: Int = remoteSocketAddress.getPort

  /** Get User-Agent header */
  def userAgent: Option[String] = {
    headers.get(Fields.UserAgent)
  }

  /** Set User-Agent header */
  def userAgent(ua: String): Request = {
    this
  }

  override def toString: String = {
    s"""Request($method $uri)"""
  }
}

abstract class RequestProxy extends Request {

  def request: Request


  override def params: Param = request.params

  override def version: Version = request.version

  override def cookies: Cookies = request.cookies

  final def method: Method = request.method

  final def method(method: Method): Request = request.method(method)

  final def uri: String = request.uri

  final def uri(u: String): Request = request.uri(u)

  override final def body: ByteBuffer = request.body

  final def remoteSocketAddress: InetSocketAddress = request.remoteSocketAddress

  override lazy val headers: Headers = request.headers
}

private[goa] class RequestWithRouterParam(val request: Request, val router: Router, val pathMatcher: PathMatcher) extends RequestProxy {

  override def params: Param = {
    val p = pathMatcher.extractUriTemplateVariables(router.path, request.path)
    val splatParam = pathMatcher.extractPathWithinPattern(router.path, request.path)
    if (splatParam != null && !splatParam.isEmpty) {
      p.put("splat", splatParam)
    }
    new RouterParam(request.params, p.toMap)
  }
}

private object Request {

  class Impl(private[this] var _method: Method,
             private[this] var _uri: String,
             private[this] var _version: Version,
             private[this] var _headers: Headers,
             private[this] var _body: ByteBuffer) extends Request {

    override def method: Method = _method

    override def uri: String = _uri

    override def method(method: Method): Request = copy(method = method)

    override def uri(uri: String): Request = copy(uri = uri)

    override def params: Param = new RequestParam(this)

    def version: Version = _version

    override def headers: Headers = _headers

    override def cookies: Cookies = Cookies(this)

    def body: ByteBuffer = _body

    override def remoteSocketAddress: InetSocketAddress = ???

    private[this] def copy(method: Method = _method,
                           uri: String = _uri,
                           version: Version = _version,
                           headers: Headers = _headers,
                           body: ByteBuffer = _body): Request = {
      new Impl(method, uri, version, headers, body)
    }

  }

  def apply(bodyReader: MessageBodyReader, httpRequest: Any): Request = {
    null
  }
}