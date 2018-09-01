package goa

import java.nio.ByteBuffer

import goa.util.BufferUtils

class Response private(private[this] var _version: Version,
                       private[this] var _status: Status,
                       private[this] var _headers: Headers,
                       private[this] var _body: ByteBuffer) extends Message {

  def version: Version = _version

  def version(version: Version): Response = {
    copy(version = version)
  }

  def status: Status = _status

  def status(status: Status): Response = {
    copy(status = status)
  }

  def headers: Headers = _headers

  def body: ByteBuffer = _body

  def body(body: ByteBuffer): Response = {
    copy(body = body)
  }

  def cookies: Cookies = {
    Cookies(this)
  }

  private[this] def copy(version: Version = _version,
                         status: Status = _status,
                         headers: Headers = _headers,
                         body: ByteBuffer = _body): Response = {
    new Response(version, status, headers, body)
  }

  override def toString: String = {
    s"""Response($status)"""
  }

}

object Response {

  def apply(version: Version = Version.Http11,
            status: Status = Status.Ok,
            headers: Headers = Headers.empty,
            body: ByteBuffer = BufferUtils.emptyBuffer): Response = {
    new Response(version, status, headers, body)
  }

}
