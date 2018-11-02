package goa


/**
  * Base class for @see[[Request]] and @see[[Response]]
  */
trait Message {

  /** Get the HTTP version */
  def version: Version

  def headers: Headers

  def cookies: Cookies

  def body: Buf

  def chunked: Boolean = {
    headers.get(Fields.TransferEncoding).isDefined
  }

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

}
