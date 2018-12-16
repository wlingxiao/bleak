package bleak
package netty

import matcher.PathMatcher

private[netty] class DefaultPathParams(pattern: String, path: String, pathMatcher: PathMatcher) extends PathParams {

  private val variables = pathMatcher.extractUriTemplateVariables(pattern, path)

  private val extract = pathMatcher.extractPathWithinPattern(pattern, path)

  def get(key: String): Option[String] = {
    variables.get(key)
  }

  override def getAll(key: String): Iterable[String] = {
    variables.get(key)
  }

  override def splat: Option[String] = {
    if (extract != null && extract.nonEmpty) {
      Some(extract)
    } else None
  }

  override def iterator: Iterator[(String, String)] = {
    variables.iterator
  }

  override def apply(key: String): String = get(key).orNull
}
