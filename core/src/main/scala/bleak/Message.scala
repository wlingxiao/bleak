package bleak

import io.netty.handler.codec.http.HttpVersion

/**
  * Base class for @see[[Request]] and @see[[Response]]
  */
trait Message {

  /** Get the HTTP version */
  def version: HttpVersion

  /** Get the HTTP version */
  def version(v: HttpVersion): this.type

  def headers: Headers

  def headers(h: Headers): this.type

  def cookies: Cookies

  def cookies(c: Cookies): this.type

  def content: Content

  def content(c: Content): this.type

  def header(name: CharSequence, value: Any): this.type = headers(headers.set(name, value))

  def keepAlive: Boolean

  def keepAlive(keepAlive: Boolean): this.type

  def chunked: Boolean

  def chunked(chunked: Boolean): this.type

}
