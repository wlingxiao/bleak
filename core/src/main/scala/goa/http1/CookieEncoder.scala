package goa.http1

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZoneOffset}
import java.util.{Date, Locale}

import goa.Cookie

import scala.collection.mutable.ArrayBuffer

trait CookieEncoder {

  protected val EQUALS = '='

  protected val SEMICOLON = ';'

  protected val SP = ' '

  protected def addUnquoted(sb: StringBuilder, name: String, value: String): Unit = {
    sb.append(name)
    sb.append(EQUALS)
    sb.append(value)
    sb.append(SEMICOLON)
    sb.append(SP)
  }

  protected def add(sb: StringBuilder, name: String, value: Any): Unit = {
    sb.append(name)
    sb.append(EQUALS)
    sb.append(value)
    sb.append(SEMICOLON)
    sb.append(SP)
  }

  protected def stripTrailingSeparator(buf: StringBuilder): String = {
    if (buf.nonEmpty) buf.setLength(buf.length - 2)
    buf.toString
  }

  protected def stripTrailingSeparatorOrNull(buf: StringBuilder): String = if (buf.isEmpty) null
  else stripTrailingSeparator(buf)

  def encode(cookie: Cookie): String

}

class ServerCookieEncoder extends CookieEncoder {

  override def encode(cookie: Cookie): String = {
    val buf = StringBuilder.newBuilder
    addUnquoted(buf, cookie.name, cookie.value)

    if (cookie.maxAge != Long.MinValue) {
      add(buf, CookieHeaderNames.MAX_AGE, cookie.maxAge)
      val expires = new Date(cookie.maxAge * 1000 + System.currentTimeMillis)
      addUnquoted(buf, CookieHeaderNames.EXPIRES, format(expires))
    }

    if (cookie.path != null) {
      addUnquoted(buf, CookieHeaderNames.PATH, cookie.path)
    }

    if (cookie.secure) {
      buf.append(CookieHeaderNames.SECURE)
      buf.append(SEMICOLON)
      buf.append(SP)
    }

    if (cookie.httpOnly) {
      buf.append(CookieHeaderNames.HTTPONLY)
      buf.append(SEMICOLON)
      buf.append(SP)
    }
    stripTrailingSeparator(buf)
  }


  private def format(date: Date): String = {
    val httpDateFormat = DateTimeFormatter
      .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
      .withLocale(Locale.ENGLISH)
      .withZone(ZoneId.of("GMT"))
    date.toInstant.atOffset(ZoneOffset.UTC).format(httpDateFormat)
  }

}

class ClientCookieEncoder extends CookieEncoder {

  override def encode(cookie: Cookie): String = {
    val buf = StringBuilder.newBuilder
    val value = if (cookie.rawValue != null) cookie.rawValue
    else if (cookie.value != null) cookie.value
    else ""
    addUnquoted(buf, cookie.name, value)
    stripTrailingSeparator(buf)
  }

  def encode(cookies: Iterable[Cookie]): String = {
    val cookiesIt = cookies.iterator
    if (!cookiesIt.hasNext) {
      return null
    }
    val buf = new StringBuilder
    val firstCookie = cookiesIt.next()
    if (!cookiesIt.hasNext) encode(buf, firstCookie)
    else {
      val cookieList = ArrayBuffer[Cookie]()
      cookieList += firstCookie
      while (cookiesIt.hasNext) {
        cookieList += cookiesIt.next()
      }
      cookieList.sorted(COOKIE_COMPARATOR).foreach(encode(buf, _))
    }
    stripTrailingSeparatorOrNull(buf)
  }

  private def encode(buf: StringBuilder, c: Cookie): Unit = {
    val name = c.name
    val value = c.value

    add(buf, name, value)
  }

  private val COOKIE_COMPARATOR = new Ordering[Cookie]() {
    override def compare(c1: Cookie, c2: Cookie): Int = {
      val path1 = c1.path
      val path2 = c2.path
      val len1 = if (path1 == null) Integer.MAX_VALUE
      else path1.length
      val len2 = if (path2 == null) Integer.MAX_VALUE
      else path2.length
      val diff = len2 - len1
      if (diff != 0) return diff
      -1
    }
  }

}