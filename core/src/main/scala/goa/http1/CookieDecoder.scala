package goa.http1

import java.nio.CharBuffer
import java.util
import java.util.Date
import java.util.concurrent.atomic.AtomicReference

import goa.Cookie
import goa.logging.Logging

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
    Cookie(name, unwrappedValue.toString, wrap = wrap)
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

  import ClientCookieDecoder._

  def decode(header: String): Cookie = {
    val builder: AtomicReference[CookieBuilder] = new AtomicReference[CookieBuilder]()
    parseCookie(header, 0, builder)
    builder.get().build()
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

  private def parseCookie(header: String, i: Int, builder: AtomicReference[CookieBuilder]): Unit = {
    val headerLen = header.length
    if (headerLen == i) {
      ()
    } else {
      header.charAt(i) match {
        case c if isBlank(c) => parseCookie(header, i + 1, builder)
        case _ =>
          val (pos, cookiePos) = parsePerCookie(header, i, i)
          var valueEnd = cookiePos.valueEnd
          if (valueEnd > 0 && header.charAt(valueEnd - 1) == ',') {
            valueEnd -= 1
          }
          if (builder.get() == null) {
            val cookie = createCookie(header, CookiePos(cookiePos.nameBegin, cookiePos.nameEnd, cookiePos.valueBegin, valueEnd))
            if (cookie == null) {
              ()
            } else {
              val cookieBuilder = CookieBuilder(cookie, header)
              builder.set(cookieBuilder)
            }
          } else {
            builder.get().appendAttribute(CookiePos(cookiePos.nameBegin, cookiePos.nameEnd, cookiePos.valueBegin, valueEnd))
          }
          parseCookie(header, pos, builder)
      }

    }
  }

  def isBlank(c: Char): Boolean = {
    c == '\t' || c == '\n' || c == 0x0b || c == '\f' || c == '\r' || c == ' ' || c == ',' || c == ';'
  }
}

private[goa] object ClientCookieDecoder {

  case class CookieBuilder(cookie: Cookie, header: String) {

    var domain: String = _
    var path: String = _
    var maxAge: Long = Long.MinValue
    var expiresStart: Int = _
    var expiresEnd: Int = _
    var secure: Boolean = _
    var httpOnly: Boolean = _

    def appendAttribute(pos: CookiePos): Unit = {
      val keyStart = pos.nameBegin
      val keyEnd = pos.nameEnd
      val valueStart = pos.valueBegin
      val valueEnd = pos.valueEnd

      val length = keyEnd - keyStart
      if (length == 4) {
        parse4(keyStart, valueStart, valueEnd)
      } else if (length == 6) {
        parse6(keyStart, valueStart, valueEnd)
      } else if (length == 7) {
        parse7(keyStart, valueStart, valueEnd)
      } else if (length == 8) {
        parse8(keyStart)
      }
    }

    private def parse8(nameStart: Int): Unit = {
      if (header.regionMatches(true, nameStart, CookieHeaderNames.HTTPONLY, 0, 8)) httpOnly = true
    }

    private def parse7(nameStart: Int, valueStart: Int, valueEnd: Int): Unit = {
      if (header.regionMatches(true, nameStart, CookieHeaderNames.EXPIRES, 0, 7)) {
        expiresStart = valueStart
        expiresEnd = valueEnd
      }
      else if (header.regionMatches(true, nameStart, CookieHeaderNames.MAX_AGE, 0, 7)) setMaxAge(computeValue(valueStart, valueEnd))
    }

    private def parse6(nameStart: Int, valueStart: Int, valueEnd: Int): Unit = {
      if (header.regionMatches(true, nameStart, CookieHeaderNames.DOMAIN, 0, 5)) domain = computeValue(valueStart, valueEnd)
      else if (header.regionMatches(true, nameStart, CookieHeaderNames.SECURE, 0, 5)) secure = true
    }

    private def parse4(nameStart: Int, valueStart: Int, valueEnd: Int): Unit = {
      if (header.regionMatches(true, nameStart, CookieHeaderNames.PATH, 0, 4)) path = computeValue(valueStart, valueEnd)
    }

    private def isValueDefined(valueStart: Int, valueEnd: Int): Boolean = {
      valueStart != -1 && valueStart != valueEnd
    }

    private def computeValue(valueStart: Int, valueEnd: Int): String = {
      if (isValueDefined(valueStart, valueEnd)) header.substring(valueStart, valueEnd)
      else null
    }

    private def setMaxAge(value: String): Unit = {
      try
        maxAge = Math.max(value.toLong, 0L)
      catch {
        case e1: NumberFormatException =>
      }
    }

    private def mergeMaxAgeAndExpires: Long = { // max age has precedence over expires
      if (maxAge != Long.MinValue) return maxAge
      else if (isValueDefined(expiresStart, expiresEnd)) { //Date expiresDate = DateFormatter.parseHttpDate(header, expiresStart, expiresEnd);
        val expiresDate: Date = null
        if (expiresDate != null) {
          val maxAgeMillis = expiresDate.getTime - System.currentTimeMillis
          return maxAgeMillis / 1000 + (if (maxAgeMillis % 1000 != 0) 1
          else 0)
        }
      }
      Long.MinValue
    }

    def build(): Cookie = {
      cookie.domain = domain
      cookie.path = path
      cookie.maxAge = mergeMaxAgeAndExpires
      cookie.secure = secure
      cookie.httpOnly = httpOnly
      cookie
    }
  }

}