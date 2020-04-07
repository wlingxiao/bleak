package bleak

import java.io.{File, RandomAccessFile}
import java.nio.charset.StandardCharsets

import io.netty.buffer.{ByteBuf, Unpooled}

trait Content

object Content {

  class ByteBufContent(val buf: ByteBuf) extends Content

  class FileContent(val file: File) extends Content

  def empty: Content = new ByteBufContent(Unpooled.EMPTY_BUFFER)

  implicit def string2Content(str: String): Content =
    new ByteBufContent(Unpooled.wrappedBuffer(str.getBytes(StandardCharsets.UTF_8)))

  implicit def byteArray2Content(bytes: Array[Byte]): Content =
    new ByteBufContent(Unpooled.wrappedBuffer(bytes))

  def apply(buf: ByteBuf): Content = new ByteBufContent(buf)

}
