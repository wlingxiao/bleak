package bleak

import java.io.{File, RandomAccessFile}
import java.nio.charset.StandardCharsets

import io.netty.buffer.{ByteBuf, Unpooled}

trait Content {
  def text: String
}

object Content {

  class ByteBufContent(val buf: ByteBuf) extends Content {
    override def text: String = buf.toString(StandardCharsets.UTF_8)
  }

  class StringContent(val text: String) extends Content

  class FileContent(val file: File) extends Content {
    override def text: String = throw new UnsupportedOperationException
  }

  def empty: Content = new ByteBufContent(Unpooled.EMPTY_BUFFER)

  implicit def string2Content(str: String): Content = new StringContent(str)

  implicit def byteArray2Content(bytes: Array[Byte]): Content =
    new ByteBufContent(Unpooled.wrappedBuffer(bytes))

}
