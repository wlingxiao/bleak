package bleak
package netty

import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.multipart.{FileUpload, InterfaceHttpData}
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType

private[netty] class DefaultFormFileParams(val httpRequest: HttpRequest) extends FormFileParams with MultipleParams[FileBuf] {

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

private[netty] object DefaultFormFileParams {

  val empty: FormFileParams = new FormFileParams with EmptyParams[FileBuf]

}