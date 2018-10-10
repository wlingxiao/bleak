package goa

import java.net.{InetSocketAddress, URI}

import util.AttributeMap

abstract class Request extends Message with AttributeMap {

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
  def localHost: String = localAddress.getAddress.getHostAddress

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

  /**
    * Returns the current [[Route]] associated with this request.
    * If there is no [[Route]], this method returns null
    *
    * @return the [[Route]] associate with this request or null if there is no valid route
    */
  def route: Route

  /**
    * Returns the current session associated with this request,or if the request does not
    * have a session, create one.
    */
  def session: Session

  /**
    * Returns the current [[Session]] associated with this request or,
    * if there is no current session and create is true, return a new session.
    * If create is false and the request has no valid [[Session]], this method returns null.
    *
    * @param create true to create a new session for this request if necessary;
    *               false to return null if there is no current session.
    * @return the [[Session]] associated with this request or null if create is false.
    */
  def session(create: Boolean): Session

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

  def route: Route = request.route

  def session: Session = request.session

  def session(create: Boolean): Session = request.session(create)
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

    override def method: Method = _method

    override def uri: String = _uri

    override def method(method: Method): Request = copy(method = method)

    override def uri(uri: String): Request = copy(uri = uri)

    override def params: Param = new Param.QueryParam(this)

    def version: Version = _version

    override def headers: Headers = _headers

    override def cookies: Cookies = _cookies

    def body: Buf = _body

    override def remoteAddress: InetSocketAddress = _remote

    override def localAddress: InetSocketAddress = _local

    override def route: Route = ???

    override def session: Session = ???

    override def session(create: Boolean): Session = ???

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