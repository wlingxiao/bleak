package bleak

import java.util.Properties

import scala.annotation.tailrec

class MimeType(mimeType: String) {

  var _mediaType: String = "*"
  var _subType: String = "*"

  private val parts = mimeType.trim.split("/")
  if (parts.length > 1) {
    _mediaType = parts(0)
    _subType = parts(1)
  }

  def mediaType: String = _mediaType

  def subType: String = _subType

  override def toString: String = mediaType + "/" + subType
}

trait Mimes {

  private val fileExtensionDetector = new FileExtensionDetector

  def fromFileName(fileName: String): Option[MimeType] = {
    getMostSpecificMimeType(fileExtensionDetector.getMimeType(fileName))
  }

  private def getMostSpecificMimeType(mimeTypes: Seq[MimeType]): Option[MimeType] = {
    mimeTypes.headOption
  }

}

object MimeType extends Mimes {

  val DefaultMimeType = "application/octet-stream"

  def apply(fileName: String): String = {
    fromFileName(fileName).map(_.toString).getOrElse(DefaultMimeType)
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

trait MimeTypeDetector {


}

class FileExtensionDetector extends MimeTypeDetector {

  import FileExtensionDetector._
  import util.RicherString._

  def getMimeType(fileName: String): Seq[MimeType] = {
    @tailrec
    def loop(extension: String): Seq[MimeType] = {
      if (extension.nonEmpty) {
        val types = Option(mimeTypes.get(extension))
          .getOrElse(mimeTypes.get(extension.toLowerCase()))
        if (types != null) {
          types.asInstanceOf[String].split(",").map(x => new MimeType(x))
        } else loop(getExtension(extension))
      } else Nil
    }

    loop(getExtension(fileName))
  }

  private def getExtension(fileName: String): String = {
    if (fileName.nonBlank) {
      val idx = fileName.indexOf(".")
      if (idx < 0) "" else fileName.substring(idx + 1)
    } else ""
  }

}

object FileExtensionDetector {

  import util.io._

  private val mimeTypes = new Properties()

  private def loadMimeTypesFile(): Unit = {
    using(getClass.getClassLoader.getResourceAsStream("bleak/mime.types.properties")) { in =>
      if (in != null) {
        mimeTypes.load(in)
      }
    }
  }

  loadMimeTypesFile()
}