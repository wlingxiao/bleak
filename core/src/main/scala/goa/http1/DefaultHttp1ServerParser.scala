package goa.http1

import java.nio.ByteBuffer

import goa.http1.DefaultHttp1ServerParser.RequestPrelude
import goa.util.BufferUtils

import scala.collection.immutable.VectorBuilder

object DefaultHttp1ServerParser {

  final case class RequestPrelude[HeadersT](
                                             method: String,
                                             uri: String,
                                             majorVersion: Int,
                                             minorVersion: Int,
                                             headers: Iterable[HeadersT])

}

final class DefaultHttp1ServerParser[Header](maxNonBody: Int)(
  implicit hl: HeaderLike[Header])
  extends Http1ServerParser(maxNonBody, maxNonBody, 2 * 1024) {

  private[this] var uri: String = null
  private[this] var method: String = null
  private[this] var minor: Int = -1
  private[this] var major: Int = -1
  private[this] val headers = new VectorBuilder[Header]

  private[this] def resetState(): Unit = {
    this.uri = null
    this.method = null
    this.major = -1
    this.minor = -1
    headers.clear()
  }

  override protected def submitRequestLine(
                                            methodString: String,
                                            uri: String,
                                            scheme: String,
                                            majorversion: Int,
                                            minorversion: Int
                                          ): Boolean = {
    this.uri = uri
    this.method = methodString
    this.major = majorversion
    this.minor = minorversion

    false
  }

  override protected def headerComplete(name: String, value: String): Boolean = {
    headers += hl.make(name, value)
    false
  }

  def getMinorVersion(): Int = this.minor

  override def reset(): Unit = {
    resetState()
    super.reset()
  }

  def parsePrelude(buffer: ByteBuffer): Boolean =
    if (!requestLineComplete() && !parseRequestLine(buffer)) false
    else if (!headersComplete() && !parseHeaders(buffer)) false
    else true

  def parseBody(buffer: ByteBuffer): ByteBuffer = parseContent(buffer) match {
    case null => BufferUtils.emptyBuffer
    case buff => buff
  }

  def getRequestPrelude(): RequestPrelude[Header] = {
    val hs = headers.result()
    headers.clear()

    RequestPrelude(method, uri, major, minor, hs)
  }
}
