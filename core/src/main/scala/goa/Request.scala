package goa

import java.net.{InetAddress, InetSocketAddress, URI}

import goa.http1.HttpRequest

abstract class Request extends Message {

  /**
    * Returns the HTTP method of this request
    *
    * @return the method name
    */
  def method: String

  /**
    * Sets the HTTP method of this request to the give `method`.
    *
    * @param method the specified HTTP method
    */
  def method_=(method: String): Unit

  /**
    * Sets the HTTP method of this request to the give `method`
    *
    * @param method the specified HTTP method
    * @return this request
    */
  def method(method: String): this.type = {
    this.method = method
    this
  }

  /**
    * Return the uri of this request
    */
  def uri: String

  /**
    * Sets the uri of this request
    */
  def uri_=(u: String): Unit

  /**
    * Sets the uri of this request
    */
  def uri(uri: String): this.type = {
    this.uri = uri
    this
  }

  /** Get path from uri    */
  def path: String = new URI(uri).getPath

  def params: Param = _params

  private[this] lazy val _params: Param = new RequestParam(this)

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

  override def toString: String = {
    s"""Request($method $uri)"""
  }
}

private object Request {

  private class Impl(httpRequest: HttpRequest) extends Request {

    private var _method = httpRequest.method

    private var _uri = httpRequest.url

    override def method: String = _method

    override def method_=(method: String): Unit = {
      _method = method
    }

    override def uri: String = _uri

    override def uri_=(u: String): Unit = {
      _uri = u
    }

    override def remoteSocketAddress: InetSocketAddress = ???
  }

  def apply(httpRequest: HttpRequest): Request = {
    new Impl(httpRequest)
  }

}