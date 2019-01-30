package bleak
import bleak.matcher.PathMatcher

trait PathParams extends Params[String] {
  def splat: Option[String]
}

object PathParams {

  def empty: PathParams = new PathParams with Params.EmptyParams[String] {
    override def splat: Option[String] = None
  }

  def apply(pattern: String, path: String, pathMatcher: PathMatcher) =
    new Impl(pattern, path, pathMatcher)

  private final class Impl(pattern: String, path: String, pathMatcher: PathMatcher)
      extends PathParams {
    private val variables = pathMatcher.extractUriTemplateVariables(pattern, path)
    private val extract = pathMatcher.extractPathWithinPattern(pattern, path)

    override def splat: Option[String] =
      if (extract != null && extract.nonEmpty) Some(extract)
      else None

    override def getAll(key: String): Iterable[String] =
      variables.get(key)

    override def get(key: String): Option[String] =
      variables.get(key)

    override def iterator: Iterator[(String, String)] =
      variables.iterator

    override def apply(key: String): String = get(key).orNull
  }

}
