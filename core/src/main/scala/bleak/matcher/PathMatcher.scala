package bleak.matcher

import scala.collection.mutable

private[bleak] trait PathMatcher {

  def tryMatch(pattern: String, path: String): Boolean

  def extractPathWithinPattern(pattern: String, path: String): String

  def extractUriTemplateVariables(pattern: String, path: String): mutable.HashMap[String, String]

  def getPatternComparator(path: String): Ordering[String]

}
