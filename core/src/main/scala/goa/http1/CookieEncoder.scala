package goa.http1

import java.time.{ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.{Date, Locale}

import goa.Cookie

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

  protected def stripTrailingSeparator(buf: StringBuilder): String = {
    if (buf.nonEmpty) buf.setLength(buf.length - 2)
    buf.toString
  }

  def encode(cookie: Cookie): String

}

object ServerCookieEncoder extends CookieEncoder {

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

  private def add(sb: StringBuilder, name: String, value: Long): Unit = {
    sb.append(name)
    sb.append(EQUALS)
    sb.append(value)
    sb.append(SEMICOLON)
    sb.append(SP)
  }

  private def format(date: Date): String = {
    val httpDateFormat = DateTimeFormatter
      .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
      .withLocale(Locale.ENGLISH)
      .withZone(ZoneId.of("GMT"))
    date.toInstant.atOffset(ZoneOffset.UTC).format(httpDateFormat)
  }

}

object ClientCookieEncoder extends CookieEncoder {

  override def encode(cookie: Cookie): String = {
    val buf = StringBuilder.newBuilder
    val value = if (cookie.rawValue != null) cookie.rawValue
    else if (cookie.value != null) cookie.value
    else ""
    addUnquoted(buf, cookie.name, value)
    stripTrailingSeparator(buf)
  }

}