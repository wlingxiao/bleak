package goa.http1

import java.nio.ByteBuffer

import goa.http1.BodyReader.BodyReaderOverflowException
import goa.util.BufferUtils
import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{Await, Awaitable}

class BodyReaderSpec extends Specification {

  def await[T](a: Awaitable[T]): T =
    Await.result(a, 5.seconds)

  "BodyReader.singleBuffer" should {
    "Return the empty reader if the buffer is empty" in {
      val reader = BodyReader.singleBuffer(ByteBuffer.allocate(0))
      reader.isExhausted must beTrue
    }

    "Provide the buffer on first invocation" in {
      val buf = ByteBuffer.allocate(10)
      val reader = BodyReader.singleBuffer(buf)
      reader.isExhausted must beFalse

      reader() must_== buf
      reader.isExhausted must beTrue
    }

    "discard clears the buffer" in {
      val buf = ByteBuffer.allocate(10)
      val reader = BodyReader.singleBuffer(buf)
      reader.discard()
      reader.isExhausted must beTrue
      reader().hasRemaining must beFalse
    }

  }

  "BodyReader.accumulate(max, bodyReader)" should {
    "accumulate an empty buffer" in {
      val reader = BodyReader.singleBuffer(ByteBuffer.allocate(0))
      val bytes = BodyReader.accumulate(Int.MaxValue, reader)
      bytes.remaining() must_== 0
    }

    "accumulate a single buffer" in {
      val reader = BodyReader.singleBuffer(ByteBuffer.allocate(10))
      val bytes = BodyReader.accumulate(Int.MaxValue, reader)
      bytes.remaining() must_== 10
    }

    "accumulate multiple buffers" in {
      val reader = new MultiByteReader(
        ByteBuffer.allocate(10),
        ByteBuffer.allocate(1)
      )

      val bytes = BodyReader.accumulate(Int.MaxValue, reader)
      bytes.remaining() must_== 11
    }

    "not overflow on allowed bytes" in {
      val ByteCount = 10
      val reader = BodyReader.singleBuffer(ByteBuffer.allocate(ByteCount))
      val bytes = BodyReader.accumulate(ByteCount, reader)
      bytes.remaining() must_== ByteCount
    }

    "not allow overflow" in {
      val reader = BodyReader.singleBuffer(ByteBuffer.allocate(10))
      BodyReader.accumulate(9, reader) must throwA[BodyReaderOverflowException]
    }
  }

  private class MultiByteReader(data: ByteBuffer*) extends BodyReader {
    private val buffers = new scala.collection.mutable.Queue[ByteBuffer]
    buffers ++= data

    override def discard(): Unit = synchronized {
      buffers.clear()
    }

    override def apply(): ByteBuffer = synchronized {
      if (!isExhausted) buffers.dequeue()
      else BufferUtils.emptyBuffer
    }

    override def isExhausted: Boolean = synchronized {
      buffers.isEmpty
    }
  }

}
