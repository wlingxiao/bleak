package goa

import java.net.{InetSocketAddress, URI}
import java.util.concurrent.ConcurrentHashMap

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
  def localeHost: String = localAddress.getAddress.getHostAddress

  /**
    * Returns the IP port number of current running server
    *
    * @return an integer specifying the port number
    */
  def localPort: Int = localAddress.getPort

  /** Get User-Agent header */
  def userAgent: Option[String] = {
    headers.get(Fields.UserAgent)
  }

  /** Set User-Agent header */
  def userAgent(ua: String): Request = {
    this
  }

  def attr(key: Symbol, value: Any): Request

  def attr(key: Symbol): Option[Any]

  def router: Router

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

  override final def body: Buf = request.body

  final def remoteAddress: InetSocketAddress = request.remoteAddress

  final def localAddress: InetSocketAddress = request.localAddress

  override lazy val headers: Headers = request.headers

  def attr(key: Symbol, value: Any): Request = request.attr(key, value)

  def attr(key: Symbol): Option[Any] = request.attr(key)

  def router: Router = request.router

}

private[goa] class RequestWithRouterParam(val request: Request, val pathMatcher: PathMatcher) extends RequestProxy {

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
             private[this] var _cookies: Cookies,
             private[this] var _body: Buf,
             private[this] var _remote: InetSocketAddress = null,
             private[this] var _local: InetSocketAddress = null) extends Request {

    private val attributes = new ConcurrentHashMap[Symbol, Any]()

    private[this] var _router: Router = _

    override def method: Method = _method

    override def uri: String = _uri

    override def method(method: Method): Request = copy(method = method)

    override def uri(uri: String): Request = copy(uri = uri)

    override def params: Param = new RequestParam(this)

    def version: Version = _version

    override def headers: Headers = _headers

    override def cookies: Cookies = _cookies

    def body: Buf = _body

    override def remoteAddress: InetSocketAddress = _remote

    override def localAddress: InetSocketAddress = _local

    override def attr(key: Symbol): Option[Any] = {
      Option(attributes.get(key))
    }

    override def attr(key: Symbol, value: Any): Request = {
      attributes.put(key, value)
      this
    }

    override def router: Router = _router

    def router(r: Router): Impl = {
      _router = r
      this
    }

    private[this] def copy(method: Method = _method,
                           uri: String = _uri,
                           version: Version = _version,
                           headers: Headers = _headers,
                           cookies: Cookies = _cookies,
                           body: Buf = _body): Request = {
      new Impl(method, uri, version, headers, cookies, body)
    }
  }

  def apply(bodyReader: Any, httpRequest: Any): Request = {
    null
  }
}