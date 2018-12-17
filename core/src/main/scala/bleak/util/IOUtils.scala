package bleak
package util

import java.io.{ByteArrayOutputStream, InputStream, OutputStream}

import scala.annotation.tailrec

object IOUtils {

  def using[A, B <: AutoCloseable](closeble: B)(fn: B => A): A = {
    try {
      fn(closeble)
    } finally {
      if (closeble != null) {
        closeble.close()
      }
    }
  }

  def copy(in: InputStream, out: OutputStream, bufferSize: Int = 4096): Unit = {
    using(in) { in =>
      val buf = new Array[Byte](bufferSize)

      @tailrec
      def loop(): Unit = {
        val n = in.read(buf)
        if (n >= 0) {
          out.write(buf, 0, n)
          loop()
        }
      }

      loop()
    }
  }

  def toBytes(in: InputStream): Array[Byte] = {
    using(new ByteArrayOutputStream()) { out =>
      copy(in, out)
      out.toByteArray
    }
  }

}
