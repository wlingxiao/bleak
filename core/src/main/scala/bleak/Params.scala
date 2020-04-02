package bleak

import bleak.matcher.PathMatcher
import io.netty.handler.codec.http.QueryStringDecoder

import scala.jdk.CollectionConverters._

/**
  * Request Parameter map.
  *
  * This is a multiple-map.
  *
  * Use `getAll()` to get all values for a key.
  */
trait Params {

  def getAll(key: String): Iterable[String]

  def get(key: String): Option[String] = getAll(key).headOption

  def splat: Option[String] = None

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

  class PathParams(pattern: String, path: String, pathMatcher: PathMatcher) extends Params {

    private val variables = pathMatcher.extractUriTemplateVariables(pattern, path)

    private val extract = pathMatcher.extractPathWithinPattern(pattern, path)

    override def splat: Option[String] =
      if (extract != null && extract.nonEmpty) Some(extract)
      else None

    override def getAll(key: String): Iterable[String] =
      variables.get(key)

  }

  class CombinedParams(params: Params*) extends Params {

    override def getAll(key: String): Iterable[String] =
      params.flatMap(_.getAll(key))

    override def splat: Option[String] =
      params
        .map(_.splat)
        .reduce((x, y) => x.orElse(y))
  }

  def apply(request: Request): Params = request.route match {
    case Some(value) =>
      new CombinedParams(
        new PathParams(value.path, request.path, request.app.pathMatcher),
        new QueryParams(request.uri))
    case None => new QueryParams(request.uri)
  }

}
