package goa.server
package http1

import java.nio.CharBuffer
import java.util
import java.util.concurrent.atomic.AtomicReference

import goa.Cookie
import goa.logging.Logging
import goa.util.DateUtils

import scala.annotation.tailrec
import scala.collection.mutable

private case class CookiePos(nameBegin: Int, nameEnd: Int, valueBegin: Int, valueEnd: Int)

private[goa] abstract class CookieDecoder extends Logging {

  protected def createCookie(header: String, pos: CookiePos): Cookie = {
    val nameBegin = pos.nameBegin
    val nameEnd = pos.nameEnd
    val valueBegin = pos.valueBegin
    val valueEnd = pos.valueEnd

    if (nameBegin == -1 || nameBegin == nameEnd) {
      return null
    }
    if (valueBegin == -1) {
      None
    }

    val wrappedValue = CharBuffer.wrap(header, valueBegin, valueEnd)
    val unwrappedValue = CookieDecoder.unwrapValue(wrappedValue)
    if (unwrappedValue == null) {
      return null
    }
    val name = header.substring(nameBegin, nameEnd)
    var invalidOctetPos = CookieDecoder.firstInvalidCookieNameOctet(name)
    if (invalidOctetPos >= 0) {
      log.debug(s"Skipping cookie because name '$name' contains invalid char '${name.charAt(invalidOctetPos)}'")
      return null
    }
    val wrap = unwrappedValue.length() != (valueEnd - valueBegin)
    invalidOctetPos = CookieDecoder.firstInvalidCookieValueOctet(unwrappedValue.toString)
    if (invalidOctetPos >= 0) {
      log.debug(s"Skipping cookie because value '$unwrappedValue' contains invalid char '${unwrappedValue.charAt(invalidOctetPos)}'")
      return null
    }
    Cookie(name, unwrappedValue.toString)
  }

}

private[goa] object CookieDecoder {

  private val VALID_COOKIE_VALUE_OCTETS = {
    val bits = new util.BitSet
    bits.set(0x21)
    0x23 to 0x2B foreach bits.set
    0x2D to 0x3A foreach bits.set
    0x3C to 0x5B foreach bits.set
    0x5D to 0x7E foreach bits.set
    bits
  }

  private val VALID_COOKIE_NAME_OCTETS = {
    val bits = new util.BitSet()
    32 until 127 foreach bits.set
    Array('(', ')', '<', '>', '@', ',', ';', ':', '\\', '"', '/', '[', ']', '?', '=', '{', '}', ' ', '\t') foreach (bits.set(_, false))
    bits
  }

  private def unwrapValue(cs: CharSequence): CharSequence = {
    val len = cs.length
    if (len > 0 && cs.charAt(0) == '"') if (len >= 2 && cs.charAt(len - 1) == '"') { // properly balanced
      return if (len == 2) ""
      else cs.subSequence(1, len - 1)
    }
    else return null
    cs
  }

  private def firstInvalidOctet(cs: String, bits: util.BitSet): Int = {
    for ((x, i) <- cs.zipWithIndex) {
      if (!bits.get(x)) {
        return i
      }
    }
    -1
  }

  private def firstInvalidCookieNameOctet(cs: String): Int = {
    firstInvalidOctet(cs, VALID_COOKIE_NAME_OCTETS)
  }

  private def firstInvalidCookieValueOctet(cs: String): Int = {
    firstInvalidOctet(cs, VALID_COOKIE_VALUE_OCTETS)
  }

}

private[goa] class ServerCookieDecoder extends CookieDecoder {

  /**
    * Decodes the specified Cookie HTTP header value into a set of [[Cookie]]
    *
    * @param header Cookie header value
    * @return the decoded [[Cookie]] set
    */
  def decode(header: String): Set[Cookie] = {
    val cookies = mutable.HashSet[Cookie]()
    parseCookie(header, 0, cookies)
    cookies.toSet
  }

  @tailrec
  private def parsePerCookie(header: String, j: Int, i: Int): (Int, CookiePos) = {
    val headerLen = header.length
    header.charAt(i) match {
      case ';' => (j, CookiePos(j, i, -1, -1))
      case '=' =>
        val nameEnd = i
        val h = i + 1
        if (h == headerLen) {
          (h, CookiePos(j, nameEnd, 0, 0))
        } else {
          val valueBegin = h
          val semiPos = header.indexOf(';', h)
          val valueEnd = if (semiPos > 0) semiPos else headerLen
          (valueEnd, CookiePos(j, nameEnd, valueBegin, valueEnd))
        }
      case _ =>
        val h = i + 1
        if (h == headerLen) {
          (h, CookiePos(j, headerLen, -1, -1))
        } else parsePerCookie(header, j, h)
    }
  }

  private def parseCookie(header: String, i: Int, cookies: mutable.HashSet[Cookie]): Unit = {
    val headerLen = header.length
    if (headerLen == i) {
      ()
    } else {
      header.charAt(i) match {
        case c if isBlank(c) => parseCookie(header, i + 1, cookies)
        case _ =>
          val (pos, cookiePos) = parsePerCookie(header, i, i)
          val cookie = createCookie(header, cookiePos)
          if (cookie != null) {
            cookies += cookie
          }
          parseCookie(header, pos, cookies)
      }

    }
  }

  def isBlank(c: Char): Boolean = {
    c == '\t' || c == '\n' || c == 0x0b || c == '\f' || c == '\r' || c == ' ' || c == ',' || c == ';'
  }

}

private[goa] class ClientCookieDecoder extends CookieDecoder {

  /**
    * Decodes the specified Set-Cookie HTTP header value into a [[Cookie]]
    *
    * @param header Set-Cookie HTTP header value
    * @return the decoded [[Cookie]]
    */
  def decode(header: String): Cookie = {
    val headerLen = header.length
    var name: String = null
    var mark: Int = 0

    var cookieName: String = null
    var cookieValue: String = null
    var domain: String = null
    var path: String = null
    var maxAge: Long = Long.MinValue
    var expires: String = null
    var secure: Boolean = false
    var httpOnly: Boolean = false

    def setDecodedValue(name: String, value: String): Unit = {
      val trim = name.trim
      if (trim.equalsIgnoreCase(CookieHeaderNames.SECURE)) secure = true
      else if (trim.equalsIgnoreCase(CookieHeaderNames.HTTPONLY)) httpOnly = true
      else if (trim.equalsIgnoreCase(CookieHeaderNames.DOMAIN)) domain = value
      else if (trim.equalsIgnoreCase(CookieHeaderNames.PATH)) path = value
      else if (trim.equalsIgnoreCase(CookieHeaderNames.EXPIRES)) expires = value
      else if (trim.equalsIgnoreCase(CookieHeaderNames.MAX_AGE)) {
        try
          maxAge = Math.max(value.toLong, 0L)
        catch {
          case _: NumberFormatException =>
        }
      }
      else {
        cookieName = trim
        cookieValue = value
      }
    }

    @tailrec
    def go(i: Int): Unit = {
      if (i < headerLen) {
        header.charAt(i) match {
          case c if c == '=' && name == null =>
            if (mark != i) {
              name = header.substring(mark, i)
            }
            mark = i + 1
          case c if c == ';' =>
            if (name == null && mark != i) {
              setDecodedValue(header.substring(mark, i), null)
            } else if (name != null) {
              val value = header.substring(mark, i)
              setDecodedValue(name, value)
              name = null
            }
            mark = i + 1
          case _ =>
        }
        go(i + 1)
      }
    }

    go(0)

    if (mark != headerLen) {
      if (name == null) {
        setDecodedValue(header.substring(mark, headerLen), null)
      } else {
        setDecodedValue(name, header.substring(mark, headerLen))
      }
    }

    Cookie(cookieName, cookieValue, domain, path, mergeMaxAgeAndExpires(expires, maxAge), secure, httpOnly)
  }

  private def mergeMaxAgeAndExpires(expires: String, maxAge: Long): Long = {
    if (expires != null) {
      DateUtils.parseHttpDate(expires).map { x =>
        val maxAgeMillis = x.toEpochMilli - System.currentTimeMillis
        val m = if (maxAgeMillis % 1000 != 0) 1 else 0
        maxAgeMillis / 1000 + m
      }.getOrElse(Long.MinValue)
    }
    else if (maxAge != Long.MinValue) maxAge
    else Long.MinValue
  }

}