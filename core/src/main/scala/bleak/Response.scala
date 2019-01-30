package bleak
import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders}
abstract class Response extends Message {

  def status: Status

  def status_=(status: Status): Unit

  override def toString: String =
    s"""Response($status)"""

}

object Response {

  implicit def any2Response[T](value: T)(implicit fun: T => Buf): Response = {
    val res = apply()
    res.body = fun(value)
    res
  }

  def apply(
      status: Status = Status.Ok,
      headers: Iterable[(String, String)] = Nil,
      cookies: Iterable[Cookie] = Nil,
      body: Buf = Buf.empty): Response = {
    val response = new Impl(new DefaultHttpHeaders())
    response.status = status
    for ((name, value) <- headers) {
      response.headers.add(name, value)
    }
    cookies.foreach(response.cookies.add)
    response.body = body
    response
  }

  def apply(httpHeaders: HttpHeaders): Response = new Impl(httpHeaders)

  abstract class Proxy extends Response

  private final class Impl(httpHeaders: HttpHeaders) extends Response {
    private[this] var _status: Status = Status.Ok
    private[this] var _chunked: Boolean = true

    override def status: Status = _status
    override def status_=(status: Status): Unit = _status = status

    override val headers: Headers = Headers(httpHeaders)
    lazy val cookies: Cookies = Cookies(httpHeaders, isRequest = false)

    override def chunked: Boolean = _chunked
    override def chunked_=(chunked: Boolean): Unit = _chunked = chunked
  }

}
