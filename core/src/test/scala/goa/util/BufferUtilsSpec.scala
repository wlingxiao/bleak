package goa.util

import java.nio.ByteBuffer

import org.specs2.mutable._

class BufferUtilsSpec extends Specification {

  def b(i: Int = 1) = {
    val b = ByteBuffer.allocate(4)
    b.putInt(i).flip()
    b
  }

  "BufferTools.concatBuffers" should {
    "discard null old buffers" in {
      val bb = b()
      BufferUtils.concatBuffers(null, bb) should_== bb
    }

    "discard empty buffers" in {
      val b1 = b();
      val b2 = b()
      b1.getInt()
      BufferUtils.concatBuffers(b1, b2) should_== b2
    }

    "concat two buffers" in {
      val b1 = b(1);
      val b2 = b(2)
      val a = BufferUtils.concatBuffers(b1, b2)
      a.remaining() should_== 8
      a.getInt() should_== 1
      a.getInt() should_== 2
    }

    "append the result of one to the end of another if there is room" in {
      val b1 = ByteBuffer.allocate(9)
      b1.position(1) // offset by 1 to simulated already having read a byte
      b1.putInt(1).flip().position(1)
      val b2 = b(2)

      val bb = BufferUtils.concatBuffers(b1, b2)
      bb should_== b1
      bb.position() should_== 1
      bb.getInt() should_== 1
      bb.getInt() should_== 2
    }

    "a slice of buffer a is not corrupted by concat" in {
      val a = ByteBuffer.allocate(8)
      val b = ByteBuffer.allocate(4)

      a.putInt(123).putInt(456).flip()
      b.putInt(789).flip()
      val slice = a.slice() // should contain the same view as buffer `a` right now

      a.getInt() must_== 123

      val c = BufferUtils.concatBuffers(a, b)

      slice.getInt() must_== 123
      slice.getInt() must_== 456

      c.getInt() must_== 456
      c.getInt() must_== 789
    }
  }

  "BufferTools.takeSlice" should {
    "Take a slice from a buffer" in {
      val a = ByteBuffer.allocate(10)
      a.putInt(123).putInt(456).flip()
      a.remaining() must_== 8

      val b = BufferUtils.takeSlice(a, 4)

      a.remaining must_== 4 // 4 bytes were consumed
      a.getInt() must_== 456

      b.remaining must_== 4
      b.getInt() must_== 123
    }

    "throw an `IllegalArgumentException` if you try to slice too many bytes" in {
      val a = ByteBuffer.allocate(10)
      a.putInt(123).putInt(456).flip()
      a.remaining() must_== 8

      BufferUtils.takeSlice(a, 10) must throwAn[IllegalArgumentException]
    }

    "throw an `IllegalArgumentException` if you try to slice negative bytes" in {
      val a = ByteBuffer.allocate(10)
      a.putInt(123).putInt(456).flip()
      a.remaining() must_== 8

      BufferUtils.takeSlice(a, -4) must throwAn[IllegalArgumentException]
    }
  }

  private def getBuffers(count: Int): Array[ByteBuffer] = getBuffersBase(count, false)

  private def getDirect(count: Int): Array[ByteBuffer] = getBuffersBase(count, true)

  private def getBuffersBase(count: Int, direct: Boolean): Array[ByteBuffer] = {
    (0 until count).map { i =>
      val buffer = if (direct) ByteBuffer.allocateDirect(4) else ByteBuffer.allocate(4)
      buffer.putInt(4).flip()
      buffer
    }.toArray
  }
}

