package bleak
package netty

import bleak.Status.Ok
import bleak.Version.Http11

private[netty] class NettyResponse(_headers: Headers, _cookies: Cookies) extends Response {

  @volatile
  private var _status = Ok

  @volatile
  private var _version = Http11

  @volatile
  private var _body: Buf = _

  override def version: Version = _version

  override def version_=(version: Version): Unit = {
    _version = version
  }

  override def status: Status = {
    _status
  }

  override def status_=(status: Status): Unit = {
    _status = status
  }

  override def headers: Headers = _headers

  override def cookies: Cookies = _cookies

  override def body: Buf = {
    _body
  }

  override def body_=(body: Buf): Unit = {
    _body = body
  }

  override def chunked: Boolean = ???

  override def chunked_=(chunked: Boolean): Unit = ???
}

private object NettyResponse {

  def apply(version: Version = Version.Http11,
            status: Status = Status.Ok,
            headers: Headers = DefaultHeaders.empty,
            cookies: Cookies = Cookies.empty,
            body: Buf = null): Response = {
    val res = new NettyResponse(headers, cookies)
    res.version = version
    res.status = status
    res.body = body
    res
  }

}