package bleak

import javax.activation.MimetypesFileTypeMap

object MimeType {

  private lazy val mimeTypesMap = new MimetypesFileTypeMap()

  private lazy val map = Map(
    "html" -> Html,
    "js" -> Javascript,
    "json" -> Json,
    "png" -> Png,
    "txt" -> Txt,
    "css" -> Css
  )

  def apply(filename: String): String = {
    val pos = filename.lastIndexOf(".")
    if (pos >= 0) {
      map.getOrElse(filename.substring(pos + 1).toLowerCase(), mimeTypesMap.getContentType(filename))
    } else OctetStream
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
  val Css = "text/css"
}
