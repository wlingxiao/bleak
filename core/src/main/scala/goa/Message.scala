package goa

import java.nio.ByteBuffer


/**
  * Base class for @see[[Request]] and @see[[Response]]
  */
abstract class Message {

  private[this] var _version: Version = Version.Http11

  private var _body: ByteBuffer = _

  lazy val headers: Headers = Headers.empty

  lazy val cookies: Cookies = Cookies(this)

  /** Get the HTTP version */
  def version: Version = _version

  /** Set the HTTP version */
  def version_=(version: Version): Unit = _version = version

  /** Set the HTTP version */
  def version(version: Version): this.type = {
    this.version = version
    this
  }

  def chunked: Boolean = {
    headers.get(Fields.TransferEncoding).isDefined
  }

  def chunked_=(chunked: Boolean): Unit = {
    headers.set(Fields.TransferEncoding, "chunked")
  }

  def chunked(chunked: Boolean): this.type = {
    this.chunked = chunked
    this
  }

  /** Get User-Agent header */
  def userAgent: Option[String] = {
    headers.get(Fields.UserAgent)
  }

  /** Set User-Agent header */
  def userAgent_=(userAgent: String): Unit = {
    headers.set(Fields.UserAgent, userAgent)
  }

  /** Set User-Agent header */
  def userAgent(userAgent: String): this.type = {
    this.userAgent = userAgent
    this
  }

  def keepAlive: Boolean = {
    headers.get(Fields.Connection) match {
      case Some(value) if value.equalsIgnoreCase("close") => false
      case Some(value) if value.equalsIgnoreCase("keep-alive") => true
      case _ => version == Version.Http11
    }
  }

  def keepAlive(keepAlive: Boolean): this.type = {
    this.keepAlive = keepAlive
    this
  }

  def keepAlive_=(keepAlive: Boolean): Unit = {
    if (keepAlive) headers.remove(Fields.Connection)
    else headers.set(Fields.Connection, "close")
  }

  def contentType: Option[String] = headers.get(Fields.ContentType)

  /** Set Content-Type header */
  def contentType_=(contentType: String): Unit = {
    headers.set(Fields.ContentType, contentType)
  }

  /** Set Content-Type header */
  def contentType(contentType: String): this.type = {
    this.contentType = contentType
    this
  }

  /** Get charset from Content-Type header */
  def charset: Option[String] = {
    contentType.foreach { contentType =>
      val parts = contentType.split(";")
      1 until parts.length foreach { i =>
        val part = parts(i).trim
        if (part.startsWith("charset=")) {
          val equalsIndex = part.indexOf('=')
          val charset = part.substring(equalsIndex + 1)
          return Some(charset)
        }
      }
    }
    None
  }

  /** Get media-type from Content-Type header */
  def mediaType: Option[String] = {
    contentType.flatMap { contentType =>
      val beforeSemi =
        contentType.indexOf(";") match {
          case -1 => contentType
          case n => contentType.substring(0, n)
        }
      val mediaType = beforeSemi.trim
      if (mediaType.nonEmpty)
        Some(mediaType.toLowerCase)
      else
        None
    }
  }

  def body: ByteBuffer = _body

  def body_=(body: ByteBuffer): Unit = {
    _body = body
  }

  def body(body: ByteBuffer): this.type = {
    this.body = body
    this
  }

}
