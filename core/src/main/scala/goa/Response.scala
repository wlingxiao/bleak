package goa

import java.nio.ByteBuffer

import goa.util.BufferUtils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Response private(private[this] var _version: Version,
                       private[this] var _status: Status,
                       private[this] var _headers: Headers,
                       private[this] var _cookies: Cookies,
                       private[this] var _body: Buf) extends Message {

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

  def cookies: Cookies = _cookies

  def contentType(ct: String): Response = {
    headers.set("Content-Type", ct)
    this
  }

  private[this] def copy(version: Version = _version,
                         status: Status = _status,
                         headers: Headers = _headers,
                         cookies: Cookies = _cookies,
                         body: Buf = _body): Response = {
    new Response(version, status, headers, cookies, body)
  }

  override def toString: String = {
    s"""Response($status)"""
  }

}

object Response {

  def apply(version: Version = Version.Http11,
            status: Status = Status.Ok,
            headers: Headers = Headers.empty,
            cookies: Cookies = Cookies.empty,
            body: Buf = null): Response = {
    new Response(version, status, headers, cookies, body)
  }

  class Builder(version: Version = Version.Http11,
                status: Status = Status.Ok,
                headers: mutable.Map[String, ListBuffer[String]] = mutable.Map.empty,
                cookies: mutable.Map[String, mutable.HashSet[Cookie]] = mutable.Map.empty,
                body: Buf = null) {

    /**
      * set header
      */
    def header(k: String, v: String): Builder = {
      headers(k) = ListBuffer(v)
      this
    }

    def addHeader(k: String, v: String): Builder = {
      headers(k) = headers.getOrElse(k, ListBuffer.empty) += v
      this
    }

    def cookie(k: String, v: String): Builder = {
      cookie(Cookie(k, v))
    }

    def cookie(c: Cookie): Builder = {
      cookies(c.name) = cookies.getOrElse(c.name, mutable.HashSet.empty) += c
      this
    }

    def setCookie(k: String, v: String): Builder = {
      setCookie(Cookie(k, v))
      this
    }

    def setCookie(c: Cookie): Builder = {
      cookies(c.name) = mutable.HashSet(c)
      this
    }

    def body(any: Buf = null): Response = {
      val responseHeaders = for {
        (key, value) <- headers.toSeq
        v <- value
      } yield (key, v)
      val responseCookies = cookies.values.flatten.toSet
      Response(version, status, Headers(responseHeaders: _*), Cookies(responseCookies), any)
    }

    def contentType(ct: String): Builder = {
      headers(Fields.ContentType) = ListBuffer(ct)
      this
    }

    def contentLength(len: Long): Builder = {
      headers(Fields.ContentLength) = ListBuffer(len.toString)
      this
    }

  }

}
