package bleak

import bleak.matcher.PathMatcher
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType
import io.netty.handler.codec.http.multipart.{Attribute, FileUpload, InterfaceHttpData}
import io.netty.handler.codec.http.{
  FullHttpRequest,
  HttpHeaderNames,
  HttpHeaderValues,
  HttpRequest,
  QueryStringDecoder
}

import scala.collection.mutable
import scala.jdk.CollectionConverters._

trait Params {

  def getAll(key: String): Iterable[String]

  def get(key: String): Option[String] = getAll(key).headOption

}

object Params {

  class QueryParams(uri: String) extends Params {

    private[this] val decodedParams = new QueryStringDecoder(uri).parameters()

    override def getAll(key: String): Iterable[String] = {
      val value = decodedParams.get(key)
      if (value != null) {
        value.asScala
      } else Iterable.empty
    }
  }

  class PathParams(pattern: Option[String], path: String, pathMatcher: PathMatcher) extends Params {

    private val variables =
      pattern
        .map(pathMatcher.extractUriTemplateVariables(_, path))
        .getOrElse(mutable.HashMap.empty)

    private val extract = pattern
      .map(pathMatcher.extractPathWithinPattern(_, path))
      .getOrElse("")

    def splat: Option[String] =
      if (extract != null && extract.nonEmpty) Some(extract)
      else None

    override def getAll(key: String): Iterable[String] =
      variables.get(key)

  }

  class FormParams(httpRequest: FullHttpRequest)
      extends MultipartDecoder[String](httpRequest)
      with Params {

    def isWwwForm: Boolean =
      httpRequest
        .headers()
        .contains(
          HttpHeaderNames.CONTENT_TYPE,
          HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED,
          true)

    override def getAll(name: String): Iterable[String] =
      if (isWwwForm) {
        val content = httpRequest.content().toString()
        new QueryParams(content).getAll(name)
      } else if (isMultipleForm) {
        decodeAll(name)
      } else throw new IllegalStateException()

    override def get(name: String): Option[String] =
      if (isWwwForm) {
        val content = httpRequest.content().toString()
        new QueryParams(content).get(name)
      } else if (isMultipleForm) {
        decode(name)
      } else throw new IllegalStateException()

    override def shouldHandle(data: InterfaceHttpData): Boolean =
      data.getHttpDataType == HttpDataType.Attribute

    override def handle(data: InterfaceHttpData): String = data.asInstanceOf[Attribute].getValue
  }

  class FormFileParams(httpRequest: HttpRequest) extends MultipartDecoder[FileUpload](httpRequest) {
    override def shouldHandle(data: InterfaceHttpData): Boolean =
      data.getHttpDataType == HttpDataType.FileUpload

    override def handle(data: InterfaceHttpData): FileUpload = data.asInstanceOf[FileUpload]

    def getAll(name: String): Iterable[FileUpload] = decodeAll(name)

    def get(name: String): Option[FileUpload] = decode(name)

  }
}
