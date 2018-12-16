package bleak
package netty

import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.multipart.{FileUpload, InterfaceHttpData}
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType

private[netty] class DefaultFormFileParams(val httpRequest: HttpRequest) extends FormFileParams with MultipleParams[FormFile] {

  override def apply(key: String): FormFile = get(key).orNull

  override val httpDataType: HttpDataType = {
    HttpDataType.FileUpload
  }

  override def handleData(data: InterfaceHttpData): FormFile = {
    val f = data.asInstanceOf[FileUpload]
    DefaultFormFile(f.get(), f.getFilename)
  }

}

private[netty] object DefaultFormFileParams {

  val empty: FormFileParams = new FormFileParams with EmptyParams[FormFile]

}