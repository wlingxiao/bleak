package bleak

import java.nio.charset.Charset
import java.util.Locale


/**
  * Base class for @see[[Request]] and @see[[Response]]
  */
trait Message {

  /** Get the HTTP version */
  def version: Version

  def version_=(version: Version): Unit

  def headers: Headers

  def cookies: Cookies

  def body: Buf

  def body_=(body: Buf): Unit

  def chunked: Boolean

  def chunked_=(chunked: Boolean): Unit

  def keepAlive: Boolean = {
    headers.get(Fields.Connection) match {
      case Some(value) if value.equalsIgnoreCase("close") => false
      case Some(value) if value.equalsIgnoreCase("keep-alive") => true
      case _ => version == Version.Http11
    }
  }

  def keepAlive_=(keepAlive: Boolean): Unit = {
    if (keepAlive) headers.remove(Fields.Connection)
    else headers.set(Fields.Connection, "close")
  }

  def contentType: Option[String] = headers.get(Fields.ContentType)

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

  def charset_=(cs: Charset): Unit = {
    mimeType match {
      case Some(mt) =>
        val ct = mt + ";" + cs.displayName(Locale.ENGLISH).toLowerCase(Locale.ENGLISH)
        headers.set(Fields.ContentType, ct)
      case None =>
    }
  }

  /** Get mime-type from Content-Type header */
  def mimeType: Option[String] = {
    contentType.flatMap { contentType =>
      val beforeSemi =
        contentType.indexOf(";") match {
          case -1 => contentType
          case n => contentType.substring(0, n)
        }
      val mime = beforeSemi.trim
      if (mime.nonEmpty)
        Some(mime.toLowerCase)
      else
        None
    }
  }

  def mimeType_=(tpe: String): Unit = {
    val ct = contentType.flatMap { s =>
      val pos = s.indexOf(";")
      val ret = if (pos >= 0) {
        tpe + s.substring(pos + 1)
      } else tpe
      Option(ret)
    }.getOrElse(tpe)
    headers.set(Fields.ContentType, ct)
  }

}
