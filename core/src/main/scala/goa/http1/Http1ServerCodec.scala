package goa.http1

import java.io.EOFException
import java.nio.charset.StandardCharsets
import java.nio.{ByteBuffer, CharBuffer}

import goa.logging.Logging
import goa.pipeline.Context
import goa.util.{BufferUtils, HttpHeaderUtils, SpecialHeaders}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.util.Try

private object InternalWriter {
  def ClosedChannelException: Nothing = throw new EOFException()

  val CachedSuccess = ()
  val BufferLimit = 32 * 1024
}

sealed trait RouteResult

case object Reload extends RouteResult

case object Close extends RouteResult

case class HttpResponsePrelude(code: Int, status: String, headers: Seq[(String, String)])

final class Http1ServerCodec(maxNonBodyBytes: Int, channelCtx: Context) extends Logging {

  private val CRLFBytes = "\r\n".getBytes(StandardCharsets.US_ASCII)

  private val terminationBytes =
    "\r\n0\r\n\r\n".getBytes(StandardCharsets.US_ASCII)

  private[this] val parser =
    new DefaultHttp1ServerParser[(String, String)](maxNonBodyBytes)

  private[this] val lock = parser

  private[this] var buffered: ByteBuffer = BufferUtils.emptyBuffer

  private[this] var requestId: Long = 0L

  def reset(): Unit = {
    buffered = BufferUtils.emptyBuffer
    parser.shutdownParser()
    parser.reset()
  }

  def getRequest(ctx: Context, msg: ByteBuffer): HttpRequest = {
    log.info("buffered: " + buffered.limit())
    val req = maybeGetRequest(ctx, msg)
    if (req != null) {
      req
    } else {
      readAndGetRequest(ctx, msg)
    }
  }

  private def readAndGetRequest(ctx: Context, msg: ByteBuffer): HttpRequest = {
    buffered = BufferUtils.concatBuffers(buffered, msg)
    maybeGetRequest(ctx, msg)
  }

  private def maybeGetRequest(ctx: Context, msg: ByteBuffer): HttpRequest = {
    if (parser.parsePrelude(buffered)) {
      val prelude = parser.getRequestPrelude()
      val body = getBody(ctx, msg)
      HttpRequest(
        prelude.method,
        prelude.uri,
        prelude.majorVersion,
        prelude.majorVersion,
        prelude.headers.toSeq,
        body
      )
    } else {
      null
    }
  }

  private def getBody(ctx: Context, msg: ByteBuffer): BodyReader = {
    if (parser.contentComplete()) BodyReader.EmptyBodyReader
    else new BodyReader {

      private val thisRequest = requestId

      private var discarded = false

      override def discard(): Unit = lock.synchronized {
        discarded = false
      }

      override def apply(): ByteBuffer = lock.synchronized {
        if (discarded || parser.contentComplete()) {
          BufferUtils.emptyBuffer
        } else if (thisRequest != requestId) {
          throw new EOFException()
        } else {
          val buf = parser.parseBody(buffered)
          if (buf.hasRemaining) buf
          else if (parser.contentComplete()) BufferUtils.emptyBuffer
          else {
            val buffer = msg
            buffered = BufferUtils.concatBuffers(buffered, buffer)
            apply()
          }
        }
      }

      override def isExhausted: Boolean = lock.synchronized {
        discarded || thisRequest != requestId || parser.contentComplete()
      }
    }
  }

  abstract class InternalWriter extends BodyWriter {
    final override type Finished = RouteResult

    private[this] var closed = false
    protected val lock: Object = this

    protected def doWrite(buffer: ByteBuffer): Unit

    protected def doFlush(): Unit

    protected def doClose(): RouteResult

    final override def write(buffer: ByteBuffer): Unit =
      lock.synchronized {
        if (closed) InternalWriter.ClosedChannelException
        else doWrite(buffer)
      }

    final override def flush(): Unit = lock.synchronized {
      if (closed) InternalWriter.ClosedChannelException
      else doFlush()
    }

    final override def close(): RouteResult = lock.synchronized {
      if (closed) InternalWriter.ClosedChannelException
      else {
        closed = true
        doClose()
      }
    }
  }

  def getEncoder(forceClose: Boolean, prelude: HttpResponsePrelude): InternalWriter = {
    val minorVersion = parser.getMinorVersion()
    val sb = new StringBuilder(512)
    sb.append("HTTP/1.")
      .append(minorVersion)
      .append(' ')
      .append(prelude.code)
      .append(' ')
      .append(prelude.status)
      .append("\r\n")

    val sh@SpecialHeaders(_, _, connection) =
      HttpHeaderUtils.renderHeaders(sb, prelude.headers)

    val closing = forceClose || !HttpHeaderUtils.isKeepAlive(connection, minorVersion)

    if (closing) sb.append("connection: close\r\n")
    else if (minorVersion == 0 && sh.contentLength.isDefined && Try(sh.contentLength.get.toLong).isSuccess) {
      sb.append("connection: keep-alive\r\n")
    }

    sh match {
      case SpecialHeaders(Some(te), _, _) if te.equalsIgnoreCase("chunked") =>
        if (minorVersion > 0) new ChunkedBodyWriter(closing, sb, -1)
        else new ClosingWriter(sb)

      case SpecialHeaders(_, Some(len), _) =>
        try new FixedLengthBodyWriter(closing, sb, len.toLong)
        catch {
          case ex: NumberFormatException =>
            new SelectingWriter(closing, minorVersion, sb)
        }

      case _ => new SelectingWriter(closing, minorVersion, sb)
    }
  }

  private class ChunkedBodyWriter(forceClose: Boolean,
                                  private var prelude: StringBuilder,
                                  maxCacheSize: Int) extends InternalWriter {

    prelude.append("transfer-encoding: chunked\r\n")
    private val cache = new ListBuffer[ByteBuffer]
    private var cacheSize = 0

    override protected def doWrite(buffer: ByteBuffer): Unit = {
      if (!buffer.hasRemaining) ()
      else {
        cache += buffer
        cacheSize += buffer.remaining()
        if (cacheSize > maxCacheSize) doFlush()
        else ()
      }
    }

    override protected def doFlush(): Unit = flushCache(false)

    override protected def doClose(): RouteResult = {
      flushCache(true)
      selectComplete(forceClose)
    }

    private def flushCache(last: Boolean): Unit = {
      if (last) {
        cache += ByteBuffer.wrap(terminationBytes)
      }

      var buffers = cache.result()
      cache.clear()

      if (cacheSize > 0) {
        buffers = lengthBuffer() :: buffers
        cacheSize = 0
      }

      if (prelude != null) {
        val buffer = ByteBuffer.wrap(prelude.result().getBytes(StandardCharsets.ISO_8859_1))
        prelude = null
        buffers = buffer :: buffers
      }

      if (buffers.isEmpty) {
        InternalWriter.CachedSuccess
      } else {
        buffers.foreach(channelCtx.write(_))
      }
    }

    private def lengthBuffer(): ByteBuffer = {
      val bytes =
        Integer.toHexString(cacheSize).getBytes(StandardCharsets.US_ASCII)
      val b = ByteBuffer.allocate(2 + bytes.length + 2)
      b.put(CRLFBytes).put(bytes).put(CRLFBytes).flip()
      b
    }

  }

  private class ClosingWriter(var sb: StringBuilder) extends InternalWriter {
    override protected def doWrite(buffer: ByteBuffer): Unit = {
      if (sb == null) {
        channelCtx.write(buffer)
      } else {
        sb.append("\r\n")
        val prelude = StandardCharsets.ISO_8859_1.encode(sb.result())
        channelCtx.write(prelude)
        channelCtx.write(buffer)
      }
    }

    override protected def doFlush(): Unit = {
      if (sb == null) {
        ()
      } else {
        doWrite(BufferUtils.emptyBuffer)
      }
    }

    override protected def doClose(): RouteResult = {
      if (sb == null) Close
      else {
        doFlush()
        Close
      }
    }
  }

  private class FixedLengthBodyWriter(forceClose: Boolean, sb: StringBuilder, len: Long)
    extends InternalWriter {

    private var cache = new ArrayBuffer[ByteBuffer](4)
    private var cachedBytes: Int = 0
    private var closed = false
    private var written: Long = 0L

    sb.append("content-length: ").append(len).append("\r\n\r\n")
    val prelude = StandardCharsets.ISO_8859_1.encode(CharBuffer.wrap(sb))
    cache += prelude
    cachedBytes = prelude.remaining()

    override protected def doWrite(buffer: ByteBuffer): Unit = {
      val bufSize = buffer.remaining()
      if (bufSize == 0) ()
      else if (written + bufSize > len) {
        val msg = s"StaticBodyWriter: Body overflow detected. Expected bytes: " +
          s"$len, attempted to send: ${written + bufSize}. Truncating."
        val ex = new IllegalStateException(msg)
        throw ex
      } else if (cache.isEmpty && bufSize > InternalWriter.BufferLimit) {
        assert(cachedBytes == 0, "Invalid cached bytes state")
        written += bufSize
        channelCtx.write(buffer)
      } else {
        cache += buffer
        written += bufSize
        cachedBytes += bufSize

        if (cachedBytes > InternalWriter.BufferLimit) flush()
        else InternalWriter.CachedSuccess
      }
    }

    override protected def doFlush(): Unit = {
      if (cache.nonEmpty) {
        val buffs = cache
        cache = new ArrayBuffer[ByteBuffer](math.min(16, buffs.length + 2))
        cachedBytes = 0
        buffs.foreach(channelCtx.write(_))
      } else ()
    }

    override protected def doClose(): RouteResult = {
      if (cache.nonEmpty) {
        doFlush()
        selectComplete(forceClose)
      } else {
        closed = true
        selectComplete(forceClose)
      }

    }
  }

  private class SelectingWriter(forceClose: Boolean, minor: Int, sb: StringBuilder)
    extends InternalWriter {

    private val cache = new ListBuffer[ByteBuffer]
    private var cacheSize = 0
    private var underlying: InternalWriter = null

    override protected def doWrite(buffer: ByteBuffer): Unit = {
      if (underlying != null) underlying.write(buffer)
      else {
        cache += buffer
        cacheSize += buffer.remaining()

        if (cacheSize > InternalWriter.BufferLimit) {
          // Abort caching: too much data. Create a chunked writer.
          startChunked()
        } else InternalWriter.CachedSuccess
      }

    }

    override protected def doFlush(): Unit = {
      if (underlying != null) underlying.flush()
      else {
        startChunked()
        flush()
      }
    }

    override protected def doClose(): RouteResult = {
      if (underlying != null) underlying.close()
      else {
        val buffs = cache.result()
        cache.clear()
        sb.append("content-length: ").append(cacheSize).append("\r\n\r\n")
        val prelude = StandardCharsets.US_ASCII.encode(CharBuffer.wrap(sb))
        (prelude :: buffs).foreach(channelCtx.write(_))
        selectComplete(forceClose)
      }
    }

    private[this] def startChunked(): Unit = {
      underlying = {
        if (minor > 0)
          new ChunkedBodyWriter(false, sb, InternalWriter.BufferLimit)
        else new ClosingWriter(sb)
      }

      val buff = BufferUtils.joinBuffers(cache)
      cache.clear()

      underlying.write(buff)
    }
  }

  private[this] def selectComplete(forceClose: Boolean): RouteResult =
    lock.synchronized {
      if (forceClose || !parser.contentComplete() || parser.inChunkedHeaders())
        Close
      else {
        requestId += 1
        parser.reset()
        Reload
      }
    }
}
