package bleak
import java.io.{File, IOException}

import bleak.Params.EmptyParams
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.multipart.{FileUpload, InterfaceHttpData}
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType

trait FormFileParams extends Params[FileBuf]

object FormFileParams {
  def empty: FormFileParams = new FormFileParams with EmptyParams[FileBuf]

  def apply(httpRequest: HttpRequest): FormFileParams = new Impl(httpRequest)

  private class Impl(val httpRequest: HttpRequest)
      extends FormFileParams
      with MultipleParams[FileBuf] {

    override def apply(key: String): FileBuf = get(key).orNull

    override val httpDataType: HttpDataType = {
      HttpDataType.FileUpload
    }

    override def handleData(data: InterfaceHttpData): FileBuf = {
      val upload = data.asInstanceOf[FileUpload]
      if (upload.isInMemory) {
        FormFileBuf(upload.get(), None, upload.getFilename)
      } else {
        FormFileBuf(upload.get(), Some(upload.getFile), upload.getFilename)
      }
    }
  }

  private case class FormFileBuf(bytes: Array[Byte], _file: Option[File], filename: String)
      extends FileBuf {

    def file: File =
      _file.getOrElse {
        throw new IOException("Not represented by a file")
      }

    def string: String = new String(bytes)
  }
}
