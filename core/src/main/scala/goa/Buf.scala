package goa

import java.nio.charset.{Charset, StandardCharsets}

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
