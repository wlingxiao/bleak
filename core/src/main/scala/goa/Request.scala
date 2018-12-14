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

  override def params: Params = request.params

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