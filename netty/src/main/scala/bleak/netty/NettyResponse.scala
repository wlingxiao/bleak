package bleak
package netty

private class NettyResponse private(private[this] var _version: Version,
                                    private[this] var _status: Status,
                                    private[this] var _headers: Headers,
                                    private[this] var _cookies: Cookies,
                                    private[this] var _body: Buf) extends Response {

  def version: Version = _version

  def version(version: Version): Response = {
    copy(version = version)
  }

  def status: Status = _status

  def status(status: Status): Response = {
    copy(status = status)
  }

  def headers: Headers = _headers

  def body: Buf = _body

  def body(body: Buf): Response = {
    copy(body = body)
  }

  override def headers(h: (String, String)*): Response = {
    val hs = Headers.empty ++= headers
    hs ++= h
    copy(headers = hs)
  }

  override def headers(h: Headers): Response = {
    copy(headers = h)
  }

  override def cookies(c: Cookie*): Response = {
    val cs = Cookies(Set(c: _*))
    cs ++= cookies
    copy(cookies = cs)
  }

  override def cookies(c: Cookies): Response = {
    copy(cookies = c)
  }

  def cookies: Cookies = _cookies

  private[this] def copy(version: Version = _version,
                         status: Status = _status,
                         headers: Headers = _headers,
                         cookies: Cookies = _cookies,
                         body: Buf = _body): Response = {
    new NettyResponse(version, status, headers, cookies, body)
  }
}

private object NettyResponse {

  def apply(version: Version = Version.Http11,
            status: Status = Status.Ok,
            headers: Headers = Headers.empty,
            cookies: Cookies = Cookies.empty,
            body: Buf = null): Response = {
    new NettyResponse(version, status, headers, cookies, body)
  }

}