package bleak

import io.netty.handler.codec.http._
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType
import io.netty.handler.codec.http.multipart.{HttpPostMultipartRequestDecoder, InterfaceHttpData}

import scala.collection.JavaConverters._

private trait MultipleParams[T] extends Params[T] {

  protected val httpDataType: HttpDataType

  protected val httpRequest: HttpRequest

  def checkName(data: InterfaceHttpData, name: String): Boolean = {
    data.retain()
    data.getHttpDataType == httpDataType && data.getName == name
  }

  def checkDataType(data: InterfaceHttpData): Boolean = {
    data.retain()
    data.getHttpDataType == httpDataType
  }

  def isMultipleForm: Boolean =
    HttpHeaderValues.MULTIPART_FORM_DATA.contentEqualsIgnoreCase(HttpUtil.getMimeType(httpRequest))

  def getAll(key: String): Iterable[T] =
    if (isMultipleForm) {
      val decoder = new HttpPostMultipartRequestDecoder(httpRequest)
      try {
        for (e <- decoder.getBodyHttpDatas.asScala if checkName(e, key)) yield {
          handleData(e)
        }
      } finally {
        decoder.destroy()
      }
    } else Nil

  override def get(key: String): Option[T] =
    getAll(key).headOption

  override def iterator: Iterator[(String, T)] =
    if (isMultipleForm) {
      val decoder = new HttpPostMultipartRequestDecoder(httpRequest)
      try {
        val ret = for (e <- decoder.getBodyHttpDatas.asScala if checkDataType(e)) yield {
          e.getName -> handleData(e)
        }
        ret.iterator
      } finally {
        decoder.destroy()
      }
    } else Iterator.empty

  protected def handleData(data: InterfaceHttpData): T

}
