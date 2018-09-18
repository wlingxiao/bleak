package goa.server
package http1

import java.nio.ByteBuffer

import util.BufferUtils

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

trait BodyReader {

  def discard(): Unit


  def apply(): ByteBuffer


  def isExhausted: Boolean


  def accumulate(max: Int = Int.MaxValue): ByteBuffer =
    BodyReader.accumulate(max, this)
}

object BodyReader {

  final class BodyReaderOverflowException(val max: Int, val accumulated: Long)
    extends Exception(
      s"Message body overflowed. Maximum permitted: $max, accumulated: $accumulated")


  val EmptyBodyReader: BodyReader = new BodyReader {
    override def discard(): Unit = ()

    override def apply(): ByteBuffer = BufferUtils.emptyBuffer

    override def isExhausted: Boolean = true
  }

  def singleBuffer(buffer: ByteBuffer): BodyReader =
    if (!buffer.hasRemaining) EmptyBodyReader
    else
      new BodyReader {
        private[this] var buff = buffer

        override def discard(): Unit = this.synchronized {
          buff = BufferUtils.emptyBuffer
        }

        override def isExhausted: Boolean = this.synchronized {
          !buff.hasRemaining
        }

        override def apply(): ByteBuffer = this.synchronized {
          if (buff.hasRemaining) {
            val b = buff
            buff = BufferUtils.emptyBuffer
            b
          } else BufferUtils.emptyBuffer
        }
      }

  def accumulate(max: Int, body: BodyReader): ByteBuffer = {
    require(max >= 0)

    val acc = new ArrayBuffer[ByteBuffer]

    @tailrec
    def go(bytes: Long): ByteBuffer = {
      val buff = body()
      if (buff.hasRemaining) {
        val accumulated = bytes + buff.remaining()
        if (accumulated <= max) {
          acc += buff
          go(accumulated)
        } else {
          throw new BodyReaderOverflowException(max, accumulated)
        }
      } else {
        BufferUtils.joinBuffers(acc)
      }
    }

    go(0)
  }
}