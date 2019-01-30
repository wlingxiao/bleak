package bleak
import java.nio.charset.Charset
import scala.collection.JavaConverters._
import bleak.Params.EmptyParams
import io.netty.handler.codec.http.{FullHttpRequest, HttpHeaderValues, HttpUtil, QueryStringDecoder}
import io.netty.handler.codec.http.multipart.{Attribute, InterfaceHttpData}
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType

trait FormParams extends Params[String]

object FormParams {
  val empty: FormParams = new FormParams with EmptyParams[String]

  def apply(httpRequest: FullHttpRequest): FormParams = new Impl(httpRequest)

  private class Impl(val httpRequest: FullHttpRequest)
      extends FormParams
      with MultipleParams[String] {

    override def get(key: String): Option[String] =
      getAll(key).headOption

    override def getAll(key: String): Iterable[String] =
      if (isWwwForm) {
        val params = new QueryStringDecoder(httpRequest.content().toString(charset), false)
          .parameters()
          .get(key)
        if (params != null) {
          params.asScala
        } else Nil
      } else super.getAll(key)

    private def isWwwForm: Boolean =
      HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.contentEqualsIgnoreCase(
        HttpUtil.getMimeType(httpRequest))

    private def charset: Charset =
      HttpUtil.getCharset(httpRequest)

    override def iterator: Iterator[(String, String)] =
      if (isWwwForm) {
        val params = new QueryStringDecoder(httpRequest.content().toString(charset), false)
          .parameters()
          .asScala
        val res = for {
          (k, v) <- params
          v2 <- v.asScala
        } yield {
          (k, v2)
        }
        res.iterator
      } else super.iterator

    override val httpDataType: HttpDataType = {
      HttpDataType.Attribute
    }

    override def handleData(data: InterfaceHttpData): String =
      data.asInstanceOf[Attribute].getValue

    override def apply(key: String): String = get(key).orNull
  }

}
