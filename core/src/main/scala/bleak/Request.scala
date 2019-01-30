package bleak

import java.net.{InetSocketAddress, URI}
import java.util.{ArrayList => JArrayList}

import io.netty.handler.codec.http._

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
  def method_=(method: Method): Unit

  /**
    * Return the uri of this request
    */
  def uri: String

  /**
    * Sets the uri of this request
    */
  def uri_=(uri: String): Unit

  /** Gets path from uri    */
  def path: String = new URI(uri).getPath

  def params: Params[String]

  /**
    * Gets query parameters of this request
    */
  def query: QueryParams

  /**
    * Gets named and splat(or wildcard) parameter from uri of this request.
    */
  def paths: PathParams

  /**
    * Gets string parameter from request when using `multipart/form-data`
    */
  def form: FormParams

  /**
    * Gets file parameter from request when using `multipart/form-data`
    */
  def files: FormFileParams

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
  def userAgent_=(ua: String): Unit

  /**
    * Returns the current [[Route]] associated with this request.
    * If there is no [[Route]], this method returns null
    *
    * @return the [[Route]] associate with this request or null if there is no valid route
    */
  def route: Option[Route[_, _]]

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

  def chunked: Boolean

  def chunked_=(chunked: Boolean): Unit

  override def toString: String =
    s"""Request($method $uri)"""
}

object Request {

  def apply(): Request = {
    val req = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/")
    apply(req)
  }

  def apply(req: HttpRequest): Request = new Impl(req)

  abstract class AbstractRequest extends Request {
    protected def httpHeaders: HttpHeaders
    override val headers: Headers = Headers(httpHeaders)

    override def userAgent: Option[String] =
      Option(httpHeaders.get(HttpHeaderNames.USER_AGENT))
    override def userAgent_=(ua: String): Unit =
      httpHeaders.set(HttpHeaderNames.USER_AGENT, ua)

    override val paths: PathParams = PathParams.empty
    override def params: Params[String] = Params(this)
    override def query: QueryParams = QueryParams(uri)
    override def form: FormParams = FormParams.empty
    override def files: FormFileParams = FormFileParams.empty
    override lazy val cookies: Cookies = Cookies(httpHeaders, isRequest = true)
    override def session: Session = ???
    override def session(create: Boolean): Session = ???
    override def chunked: Boolean =
      httpHeaders.contains(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, true)
    override def chunked_=(chunked: Boolean): Unit =
      if (chunked) {
        httpHeaders.set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
        httpHeaders.remove(HttpHeaderNames.CONTENT_LENGTH)
      } else {
        val encoding = httpHeaders.getAll(HttpHeaderNames.TRANSFER_ENCODING)
        if (!encoding.isEmpty) {
          val values = new JArrayList[String](encoding)
          val it = values.iterator
          while (it.hasNext) {
            val value = it.next()
            if (HttpHeaderValues.CHUNKED.contentEqualsIgnoreCase(value)) {
              it.remove()
            }
          }
          if (values.isEmpty) {
            httpHeaders.remove(HttpHeaderNames.TRANSFER_ENCODING)
          } else {
            httpHeaders.set(HttpHeaderNames.TRANSFER_ENCODING, values)
          }
        }
      }
  }

  final class Impl(req: HttpRequest) extends AbstractRequest {
    override def httpHeaders: HttpHeaders = req.headers()
    override def method: Method = Method(req.method().name())
    override def method_=(method: Method): Unit = req.setMethod(HttpMethod.valueOf(method.name))
    override def uri: String = req.uri()
    override def uri_=(uri: String): Unit = req.setUri(uri)
    override val remoteAddress: InetSocketAddress = new InetSocketAddress(0)
    override val localAddress: InetSocketAddress = new InetSocketAddress(0)
    override def route: Option[Route[_, _]] = None
  }

  abstract class Proxy extends Request {
    def request: Request
    override def method: Method = request.method
    override def method_=(method: Method): Unit = request.method_=(method)
    override def uri: String = request.uri
    override def uri_=(uri: String): Unit = request.uri_=(uri)
    override def params: Params[String] = request.params
    override def query: QueryParams = request.query
    override def paths: PathParams = request.paths
    override def form: FormParams = request.form
    override def files: FormFileParams = request.files
    override def remoteAddress: InetSocketAddress = request.remoteAddress
    override def localAddress: InetSocketAddress = request.localAddress
    override def userAgent: Option[String] = request.userAgent
    override def userAgent_=(ua: String): Unit = request.userAgent_=(ua)
    override def route: Option[Route[_, _]] = request.route
    override def chunked: Boolean = request.chunked
    override def chunked_=(chunked: Boolean): Unit = request.chunked_=(chunked)
    override def headers: Headers = request.headers
    override def cookies: Cookies = request.cookies
    override def body: Buf = request.body
    override def body_=(body: Buf): Unit = request.body_=(body)
    override def session: Session = request.session
    override def session(create: Boolean): Session = request.session(create)
  }

}
