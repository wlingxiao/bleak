package bleak

import java.io.File
import java.nio.charset.{Charset, StandardCharsets}
import java.nio.file.Files

trait Buf {

  def string: String

  def string(charset: Charset): String = new String(bytes, charset)

  def bytes: Array[Byte]

}

object Buf {

  class Impl(val bytes: Array[Byte], charset: Charset) extends Buf {
    override def string: String = new String(bytes, charset)
  }

  def apply(bytes: Array[Byte], charset: Charset = StandardCharsets.UTF_8): Buf = {
    new Impl(bytes, charset)
  }

}

trait FileBuf extends Buf {
  def file: File

  def filename: String
}

object FileBuf {

  def apply(file: File): FileBuf = {
    new Impl(file)
  }

  class Impl(val file: File) extends FileBuf {

    override def filename: String = {
      file.getName
    }

    override def string: String = new String(bytes)

    override def bytes: Array[Byte] = Files.readAllBytes(file.toPath)
  }

}