package goa.server
package http1

import java.time.format.DateTimeFormatter
import java.time.{Instant, ZoneId}
import java.util.Locale

import scala.annotation.tailrec
import scala.collection.BitSet

object HeaderNames {
  val Connection = "connection"
  val ContentLength = "content-length"
  val ContentType = "content-type"
  val Date = "date"
  val TE = "te"
  val TransferEncoding = "transfer-encoding"

  def validH2HeaderKey(str: String): Boolean = {
    val s = str.length()

    @tailrec
    def go(i: Int): Boolean =
      if (i < s) validChars(str.charAt(i).asInstanceOf[Int]) && go(i + 1)
      else true

    s > 0 && go(0)
  }

  private val validChars = BitSet(
    (('0' to '9') ++ ('a' to 'z') ++ "!#$%&'*+-.^_`|~").map(_.toInt): _*)
}

case class SpecialHeaders(
                           transferEncoding: Option[String],
                           contentLength: Option[String],
                           connection: Option[String])

object HttpHeaderUtils {

  def renderHeaders[H: HeaderLike](sb: StringBuilder, headers: Iterable[H]): SpecialHeaders = {
    var transferEncoding: Option[String] = None
    var contentLength: Option[String] = None
    var connection: Option[String] = None
    var hasDateheader = false

    val hl = HeaderLike[H]
    val it = headers.iterator

    while (it.hasNext) {
      val header = it.next()
      val k = hl.getKey(header)
      val v = hl.getValue(header)

      if (k.equalsIgnoreCase(HeaderNames.TransferEncoding)) {
        transferEncoding = Some(v)
      } else if (k.equalsIgnoreCase(HeaderNames.ContentLength)) {
        contentLength = Some(v)
      } else if (k.equalsIgnoreCase(HeaderNames.Connection)) {
        connection = Some(v)
      } else {
        if (!hasDateheader && k.equalsIgnoreCase(HeaderNames.Date)) {
          hasDateheader = true
        }

        sb.append(k)
        if (v.length > 0) sb.append(": ").append(v)
        sb.append("\r\n")
      }
    }

    if (!hasDateheader) sb.append(getDateHeader())

    SpecialHeaders(
      transferEncoding,
      contentLength,
      connection
    )
  }

  private case class CachedDateHeader(acquired: Long, header: String)

  private val dateFormat =
    DateTimeFormatter
      .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz")
      .withLocale(Locale.US)
      .withZone(ZoneId.of("GMT"))

  @volatile
  private var dateTime = CachedDateHeader(0L, "")

  private def getDateHeader(): String = {
    val cached = dateTime
    val current = System.currentTimeMillis()
    if (current - cached.acquired <= 1000) cached.header
    else {
      val next = "date: " + dateFormat.format(Instant.now()) + "\r\n"
      dateTime = CachedDateHeader(current, next)
      next
    }
  }

  def isKeepAlive(connectionHeader: Option[String], minorVersion: Int): Boolean =
    connectionHeader match {
      case Some(headerValue) =>
        if (headerValue.equalsIgnoreCase("keep-alive")) true
        else if (headerValue.equalsIgnoreCase("close")) false
        else if (headerValue.equalsIgnoreCase("upgrade")) true
        else false
      case None => minorVersion != 0
    }
}
