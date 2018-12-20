package bleak
package swagger3

import java.net.URI
import java.nio.file.attribute.FileTime
import java.nio.file.{FileSystem, FileSystems, Files}
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

import bleak.util.{DateUtils, IOUtils}

class SwaggerUIRouter(apiDocs: String = "api-docs") extends Router {

  import SwaggerUIRouter._

  private val swaggerFilePath = "META-INF/resources/webjars/swagger-ui/"

  private val indexHtml = "index.html"

  private val fileCache = new ConcurrentHashMap[String, FileInfo]()

  private val lock = getClass

  private def httpCacheSeconds = Int.MaxValue

  def classLoader: ClassLoader = {
    Thread.currentThread().getContextClassLoader
  }

  private val srcUrl = classLoader.getResource(swaggerFilePath)

  if (srcUrl == null) {
    throw new IllegalStateException("Cannot detect swagger-ui dependency from classpath")
  }

  private def newFileSystem(uri: URI): FileSystem = {
    val env = new java.util.HashMap[String, String]()
    FileSystems.newFileSystem(uri, env)
  }

  private def hasCache(filename: String): Boolean = {
    fileCache.containsKey(filename)
  }

  private def hasIndexCache: Boolean = {
    hasCache(indexHtml)
  }

  get("/swagger-ui.html")(serveIndex _)

  private def serveIndex(ctx: Context): Result = {
    if (!hasIndexCache) {
      lock.synchronized {
        if (!hasIndexCache) {
          val indexFileInfo = loadFileInfo(indexHtml)
          if (indexFileInfo != null) {
            val bytes = convertIndexHtml(indexFileInfo, ctx)
            putCache(indexHtml, bytes)
          } else return NotFound()
        }
      }
    }
    buildResult(indexHtml, ctx, checkNotModified = false)
  }

  private def putCache(filename: String, fileInfo: FileInfo): Unit = {
    fileCache.put(filename, fileInfo)
  }

  private def convertIndexHtml(fileInfo: FileInfo, ctx: Context): FileInfo = {
    if (fileInfo != null) {
      val bytes = new String(fileInfo.bytes)
        .replaceFirst("https://petstore.swagger.io/v2/swagger.json", apiDocs)
        .replaceAll("""<script src="./""", """<script src="webjars/""")
        .replaceAll("""href="./swagger-ui.css"""", """href="webjars/swagger-ui.css"""")
        .replaceAll("""href="./favicon""", """href="webjars/favicon""")
        .getBytes()
      fileInfo.copy(bytes = bytes)
    } else null
  }

  private def loadFileInfo(filename: String): FileInfo = {
    IOUtils.using(newFileSystem(srcUrl.toURI)) { fs =>
      IOUtils.using(Files.newDirectoryStream(fs.getPath(swaggerFilePath))) { stream =>
        val it = stream.iterator()
        if (it.hasNext) {
          val target = it.next()
          val targetFile = target.resolve(filename)
          if (targetFile != null || Files.exists(targetFile)) {
            val lastModified = Files.getLastModifiedTime(targetFile)
            FileInfo(targetFile.getFileName.toString, lastModified, Files.readAllBytes(targetFile))
          } else null
        } else null
      }
    }
  }

  private def buildResult(filename: String, ctx: Context, checkNotModified: Boolean): Result = {
    val target = fileCache.get(filename)
    if (target != null) {
      val now = Instant.now()
      val date = DateUtils.formatHttpDate(Instant.now())
      if (checkNotModified && notModified(target, ctx)) {
        return Status(s = Status.NotModified, headers = Map(Fields.Date -> date))
      }
      val expires = DateUtils.formatHttpDate(now.plusSeconds(httpCacheSeconds))
      val lastModified = DateUtils.formatHttpDate(target.lastModified.toInstant)
      val cacheControl = "private, max-age=" + httpCacheSeconds
      val size = target.bytes.length.toString
      val headers = Map(
        Fields.ContentType -> MimeType(filename),
        Fields.Date -> date,
        Fields.Expires -> expires,
        Fields.LastModified -> lastModified,
        Fields.CacheControl -> cacheControl,
        Fields.ContentLength -> size)
      Ok(target.bytes, headers = headers)
    } else NotFound()
  }

  get("/webjars/**")(serveWebJars _)

  private def serveWebJars(ctx: Context): Result = {
    ctx.request.paths.splat match {
      case Some(splat) =>
        if (!hasCache(splat)) {
          lock.synchronized {
            if (!hasCache(splat)) {
              val file = loadFileInfo(splat)
              if (file != null) {
                putCache(splat, file)
              } else return NotFound()
            }
          }
        }
        buildResult(splat, ctx, checkNotModified = true)
      case None => NotFound()
    }
  }

  private def notModified(fileInfo: FileInfo, ctx: Context): Boolean = {
    ctx.request.headers.get(Fields.IfModifiedSince) match {
      case Some(str) =>
        DateUtils.parseHttpDate(str) match {
          case Some(date) =>
            val lastModified = fileInfo.lastModified
            lastModified.toMillis / 1000 == date.toEpochMilli / 1000
          case None => false
        }
      case None => false
    }
  }

}

private object SwaggerUIRouter {

  private case class FileInfo(filename: String, lastModified: FileTime, bytes: Array[Byte])

}