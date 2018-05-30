package goa

import java.net.{InetAddress, InetSocketAddress, URI}

import goa.http1.HttpRequest
import goa.matcher.PathMatcher

import scala.collection.JavaConverters._

abstract class Request extends Message {

  /**
    * Returns the HTTP method of this request
    *
    * @return the method name
    */
  def method: Method

  /**
    * Sets the HTTP method of this request to the give `method`.
    *
    * @param method the specified HTTP method
    */
  def method_=(method: Method): Unit

  /**
    * Sets the HTTP method of this request to the give `method`
    *
    * @param method the specified HTTP method
    * @return this request
    */
  def method(method: Method): this.type = {
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

abstract class RequestProxy extends Request {

  def request: Request

  final def method: Method = request.method

  final def method_=(method: Method): Unit = request.method_=(method)

  final def uri: String = request.uri

  final def uri_=(u: String): Unit = request.uri_=(u)

  final def remoteSocketAddress: InetSocketAddress = request.remoteSocketAddress
}

class RequestWithRouterParam(val request: Request, val router: Route, val pathMatcher: PathMatcher) extends RequestProxy {
  override def params: Param = {
    val p = pathMatcher.extractUriTemplateVariables(router.path, request.path)
    val splatParam = pathMatcher.extractPathWithinPattern(router.path, request.path)
    if (splatParam != null && !splatParam.isEmpty) {
      p.put("splat", splatParam)
    }
    new RouterParam(request.params, p.asScala.toMap)
  }
}

private object Request {

  private class Impl(httpRequest: HttpRequest) extends Request {

    private var _method = Method(httpRequest.method)

    private var _uri = httpRequest.url

    override def method: Method = _method

    override def method_=(method: Method): Unit = {
      _method = method
    }

    override def uri: String = _uri

    override def uri_=(u: String): Unit = {
      _uri = u
    }

    override def remoteSocketAddress: InetSocketAddress = ???
  }

  def apply(httpRequest: HttpRequest): Request = {
    val req = new Impl(httpRequest)
    req.header(httpRequest.headers.toMap)
    req.body = httpRequest.body()
    req
  }
}