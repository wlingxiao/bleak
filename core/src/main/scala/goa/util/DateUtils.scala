package goa.util

import java.time.{Instant, ZoneId, ZoneOffset}
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.util.Locale

import goa.logging.Logging

object DateUtils extends Logging {

  private val HttpDateFormatter = DateTimeFormatter
    .ofPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
    .withLocale(Locale.ENGLISH)
    .withZone(ZoneId.of("GMT"))

  def formatHttpDate(date: Instant): String = {
    date.atOffset(ZoneOffset.UTC).format(HttpDateFormatter)
  }

  def parseHttpDate(str: String): Option[Instant] = {
    try {
      Some(Instant.from(HttpDateFormatter.parse(str)))
    } catch {
      case e: DateTimeParseException =>
        log.error("parse date error", e)
        None
    }
  }

}
