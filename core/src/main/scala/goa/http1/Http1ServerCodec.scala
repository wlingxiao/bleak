package goa.http1

import java.io.EOFException
import java.nio.ByteBuffer

import goa.pipeline.Context
import goa.utils.BufferUtils


final class Http1ServerCodec(maxNonBodyBytes: Int) {

  private[this] val parser =
    new DefaultHttp1ServerParser[(String, String)](maxNonBodyBytes)

  private[this] val lock = parser

  private[this] var buffered: ByteBuffer = BufferUtils.emptyBuffer

  private[this] var requestId: Long = 0L

  def getRequest(ctx: Context, msg: ByteBuffer): HttpRequest = {
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

}
