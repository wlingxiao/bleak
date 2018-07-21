package goa

import java.net.{InetAddress, InetSocketAddress, URI}
import java.nio.ByteBuffer

import goa.http1.HttpRequest
import goa.marshalling.{DefaultMessageBodyReader, MessageBodyReader, ObjectMapper}
import goa.matcher.PathMatcher

import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.reflect.classTag

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

  private[this] lazy val _bodyParam: RequestBodyParam = new RequestBodyParam(this)

  def bodyParam: Param = _bodyParam

  private[this] lazy val _queryParam: QueryStringParam = new QueryStringParam(this)

  def queryParam: Param = _queryParam

  private[this] lazy val _params: Param = new RequestParam(_queryParam, _bodyParam)

  def params: Param = _params

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

  def extract[T: ClassTag]: Option[T]

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

  override final def body: ByteBuffer = request.body

  final def remoteSocketAddress: InetSocketAddress = request.remoteSocketAddress

  final def extract[T: ClassTag]: Option[T] = request.extract[T]

  override lazy val headers: Headers = request.headers
}

private[goa] class RequestWithRouterParam(val request: Request, val router: Route, val pathMatcher: PathMatcher) extends RequestProxy {
  override def params: Param = {
    val p = pathMatcher.extractUriTemplateVariables(router.path, request.path)
    val splatParam = pathMatcher.extractPathWithinPattern(router.path, request.path)
    if (splatParam != null && !splatParam.isEmpty) {
      p.put("splat", splatParam)
    }
    new RoutePathParam(request.params, p.toMap)
  }
}

private object Request {

  private class Impl(bodyReader: MessageBodyReader, httpRequest: HttpRequest) extends Request {

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


    override def extract[T: ClassTag]: Option[T] = {
      bodyReader.parse[T](this)
    }

    override def remoteSocketAddress: InetSocketAddress = ???
  }

  def apply(bodyReader: MessageBodyReader, httpRequest: HttpRequest): Request = {
    val req = new Impl(bodyReader, httpRequest)
    req.version = Version(httpRequest.majorVersion, httpRequest.minorVersion)
    httpRequest.headers.foreach { x =>
      req.headers.add(x._1, x._2)
    }
    req.body = httpRequest.body()
    req
  }
}