package bleak

import java.net.URI
import java.nio.file._
import java.nio.file.attribute.FileTime
import java.time.Instant

import bleak.util.{DateUtils, IOUtils}

import scala.collection.mutable.ArrayBuffer

class StaticFileRouter(filePath: String,
                       urlPath: String = "/static/**",
                       dirAllowed: Boolean = true) extends Router with DirectoryStream.Filter[Path] {

  private val lock = getClass

  private val ClasspathPrefix = "classpath:"

  protected def httpCacheSeconds: Long = Int.MaxValue

  private def headTemplate(title: String): String =
    s"""<!DOCTYPE html>
       |<html lang="en">
       |<head>
       |    <meta charset="UTF-8">
       |    <title>$title</title>
       |</head>
       |<body>
       |<table style="width: 100%">
       |    <thead>
       |    <tr>
       |        <th colspan="3" style="text-align: left; font-size: 150%; border: solid 1px">Listing of: $title</th>
       |    </tr>
       |    <tr style="text-align: left">
       |        <th>Name</th>
       |        <th>Last Modified</th>
       |        <th>Size</th>
       |    </tr>
       |    </thead>""".stripMargin

  get(urlPath) { ctx =>
    val request = ctx.request
    val splat = request.paths.splat
    splat match {
      case Some(str) =>
        serve(str, request)
      case None =>
        if (dirAllowed) {
          serveDir("", request)
        } else NotFound()
    }
  }

  private def serve(splat: String, request: Request): Result = {
    if (dirAllowed) {
      serveDir(splat, request)
    } else {
      serveFile(splat, request)
    }
  }

  protected def classLoader: ClassLoader = {
    Thread.currentThread().getContextClassLoader
  }

  private def realPath: String = {
    if (inClasspath) {
      filePath.substring(ClasspathPrefix.length)
    } else filePath
  }

  private def inClasspath: Boolean = {
    filePath.startsWith(ClasspathPrefix)
  }

  private def serveFile(splat: String, request: Request): Result = {
    if (inClasspath) {
      val path = realPath + splat
      val url = classLoader.getResource(path)
      if (url != null) {
        if (url.getProtocol == "jar") {
          serveFileInJar(url.toURI, path, request)
        } else if (url.getProtocol == "file") {
          serveFileInFileSystem(Paths.get(url.toURI), request)
        } else NotFound()
      } else NotFound()
    } else {
      val path = Paths.get(realPath, splat.split("/"): _*)
      serveFileInFileSystem(path, request)
    }
  }

  private def serveDir(splat: String, request: Request): Result = {
    if (inClasspath) {
      val path = realPath + splat
      val url = classLoader.getResource(path)
      if (url != null) {
        if (url.getProtocol == "jar") {
          serveDirInJar(url.toURI, path, splat, request)
        } else if (url.getProtocol == "file") {
          serveFileInFileSystem(Paths.get(url.toURI), request)
        } else NotFound()
      } else NotFound()
    } else {
      val path = Paths.get(realPath, splat.split("/"): _*)
      serveDirInFileSystem(path, splat, request)
    }
  }

  private def serveFileInFileSystem(path: Path, request: Request): Result = {
    if (Files.exists(path) && !Files.isDirectory(path) && accept(path)) {
      buildResult(path, request)
    } else NotFound()
  }

  private def newFileSystem(uri: URI): FileSystem = {
    val env = new java.util.HashMap[String, String]()
    FileSystems.newFileSystem(uri, env)
  }

  private def serveFileInJar(uri: URI, absPath: String, request: Request): Result = {
    lock.synchronized {
      IOUtils.using(newFileSystem(uri)) { fs =>
        val file = fs.getPath(absPath)
        if (Files.exists(file) && !Files.isDirectory(file) && accept(file)) {
          buildResult(file, request)
        } else NotFound()
      }
    }
  }

  private def serveDirInJar(uri: URI, absPath: String, splat: String, request: Request): Result = {
    lock.synchronized {
      IOUtils.using(newFileSystem(uri)) { fs =>
        val file = fs.getPath(absPath)
        if (Files.isDirectory(file)) {
          buildResult(render(file, splat))
        } else {
          if (Files.exists(file) && !Files.isDirectory(file) && accept(file)) {
            buildResult(file, request)
          } else NotFound()
        }
      }
    }
  }

  private def serveDirInFileSystem(path: Path, splat: String, request: Request): Result = {
    if (Files.isDirectory(path)) {
      buildResult(render(path, splat))
    } else serveFileInFileSystem(path, request)
  }

  private def render(path: Path, dirPath: String): String = {
    IOUtils.using(Files.newDirectoryStream(path, this)) { stream =>
      val buf = ArrayBuffer[Path]()
      stream.forEach(buf.+=)
      val res = buf.sorted(sort)
      renderHtml(res.iterator, dirPath)
    }
  }

  private def renderHtml(it: Iterator[Path], dirPath: String): String = {
    val head = if (dirPath.isEmpty) {
      headTemplate("/")
    } else {
      headTemplate("/" + dirPath)
    }
    val buf = new StringBuilder(head)
    if (dirPath.nonEmpty) {
      buf.append(s""" <tr><td><a href="../">..</a></td><td>${System.currentTimeMillis()}</td><td>--</td></tr> """)
    }
    while (it.hasNext) {
      val file = it.next()
      val isDirectory = Files.isDirectory(file)
      buf.append("<tr>")
      buf.append("<td>")
        .append("<a href=\"")
        .append(formatFileHref(file))

      buf.append("\">")
        .append(formatFileName(file))
        .append("</a>")
        .append("</td>")

      buf.append("<td>")
        .append(formatLastModifiedTime(Files.getLastModifiedTime(file)))
        .append("</td>")

      buf.append("<td>")
      if (isDirectory) {
        buf.append("--")
      } else buf.append(formatFileSize(Files.size(file)))
      buf.append("</td>")
      buf.append("</tr>")
    }
    buf.append(
      """</table>
        |</body>
        |</html>""".stripMargin)
    buf.toString()
  }

  private def formatFileHref(file: Path): String = {
    val fileName = file.getFileName.toString
    if (Files.isDirectory(file) && !fileName.endsWith("/")) {
      fileName + "/"
    } else fileName
  }

  private def buildResult(file: Path, request: Request): Result = {
    val now = Instant.now()
    val date = DateUtils.formatHttpDate(Instant.now())
    if (notModified(file, request)) {
      Status(s = Status.NotModified, headers = Map(Fields.Date -> date))
    } else {
      val expires = DateUtils.formatHttpDate(now.plusSeconds(httpCacheSeconds))
      val lastModified = DateUtils.formatHttpDate(Files.getLastModifiedTime(file).toInstant)
      val cacheControl = "private, max-age=" + httpCacheSeconds
      val headers = Map(
        Fields.ContentType -> mimeType(file),
        Fields.Date -> date,
        Fields.Expires -> expires,
        Fields.LastModified -> lastModified,
        Fields.CacheControl -> cacheControl,
        Fields.ContentLength -> Files.size(file).toString)

      try {
        Ok(file.toFile, headers = headers)
      } catch {
        case _: UnsupportedOperationException =>
          Ok(Files.readAllBytes(file), headers = headers)
      }
    }
  }

  private def notModified(file: Path, request: Request): Boolean = {
    request.headers.get(Fields.IfModifiedSince) match {
      case Some(str) =>
        DateUtils.parseHttpDate(str) match {
          case Some(date) =>
            val lastModified = Files.getLastModifiedTime(file)
            lastModified.toMillis / 1000 == date.toEpochMilli / 1000
          case None => false
        }
      case None => false
    }
  }

  private def buildResult(html: String): Result = {
    Ok(html, Map(Fields.ContentType -> MimeType.Html))
  }

  protected def formatFileName(file: Path): String = {
    file.getFileName.toString
  }

  protected def formatLastModifiedTime(time: FileTime): String = {
    time.toString
  }

  protected def formatFileSize(size: Long): String = {
    size.toString
  }

  protected def mimeType(file: Path): String = {
    val fileName = file.getFileName.toString
    MimeType(fileName)
  }

  protected def sort(x: Path, y: Path): Int = {
    if (Files.isDirectory(x) != Files.isDirectory(y)) {
      if (Files.isDirectory(x)) -1 else 1
    } else formatFileName(x).toLowerCase().compareTo(formatFileName(y).toLowerCase)
  }

  def accept(file: Path): Boolean = {
    true
  }
}
