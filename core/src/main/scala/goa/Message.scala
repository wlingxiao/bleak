package goa

import java.nio.ByteBuffer
import java.nio.charset.{Charset, StandardCharsets}

import goa.util.BufferUtils

import scala.collection.mutable


/**
  * Base class for @see[[Request]] and @see[[Response]]
  */
abstract class Message {

  private[this] var _version: Version = Version.Http11

  protected[this] val headerMap: mutable.Map[String, String] = mutable.HashMap[String, String]()

  private var _body: ByteBuffer = _

  def headers: Headers

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
    headerMap.get(Fields.UserAgent)
  }

  /** Set User-Agent header */
  def userAgent_=(userAgent: String): Unit = {
    headerMap(Fields.UserAgent) = userAgent
  }

  /** Set User-Agent header */
  def userAgent(userAgent: String): this.type = {
    this.userAgent = userAgent
    this
  }

  def keepAlive: Boolean = {
    headerMap.get(Fields.Connection) match {
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
    if (keepAlive) headerMap.remove(Fields.Connection)
    else headerMap(Fields.Connection) = "close"
  }

  def contentType: Option[String] = headerMap.get(Fields.ContentType)

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

  lazy val cookies: Cookies = Cookies(this)

  def body: ByteBuffer = _body

  def body_=(body: ByteBuffer): Unit = {
    _body = body
  }

  def body(body: ByteBuffer): this.type = {
    this.body = body
    this
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

  def contentString: String = {
    val encoding = charset.map(Charset.forName).getOrElse(StandardCharsets.UTF_8)
    BufferUtils.bufferToString(body, encoding)
  }
}
