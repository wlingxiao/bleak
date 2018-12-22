package bleak
package netty

import java.io.{File, IOException}

private[netty] case class FormFileBuf(bytes: Array[Byte],
                                      _file: Option[File],
                                      filename: String) extends FileBuf {

  def file: File = {
    _file.getOrElse {
      throw new IOException("Not represented by a file")
    }
  }

  def string: String = new String(bytes)
}
