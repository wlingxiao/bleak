package bleak

object MimeType {

  private lazy val map = Map(
    "html" -> "text/html",
    "js" -> "application/javascript",
    "json" -> "application/json",
    "png" -> "image/png",
    "txt" -> "text/plain",
    "css" -> "text/css"
  )

  def apply(extension: String): String = {
    map.getOrElse(extension.toLowerCase(), "application/octet-stream")
  }

  val Atom = "application/atom+xml"
  val Csv = "application/csv"
  val Gif = "image/gif"
  val Html = "text/html"
  val Iframe = "application/iframe"
  val Javascript = "application/javascript"
  val Jpeg = "image/jpeg"
  val Json = "application/json"
  val JsonPatch = "application/json-patch+json"
  val MultipartForm = "multipart/form-data"
  val OctetStream = "application/octet-stream"
  val PlainText = "text/plain"
  val Png = "image/png"
  val Rss = "application/rss+xml"
  val Txt = "text/plain"
  val WwwForm = "application/x-www-form-urlencoded"
  val Xls = "application/vnd.ms-excel"
  val Xml = "application/xml"
  val Zip = "application/zip"
}
