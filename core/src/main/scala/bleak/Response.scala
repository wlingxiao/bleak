package bleak

abstract class Response extends Message {

  def version: Version

  def version(version: Version): Response

  def status: Status

  def status(status: Status): Response

  override def headers: Headers

  def headers(h: (String, String)*): Response

  def headers(h: Headers): Response

  def cookies: Cookies

  def cookies(c: Cookie*): Response

  def cookies(c: Cookies): Response

  override def body: Buf

  def body(body: Buf): Response

  override def toString: String = {
    s"""Response($status)"""
  }

}
