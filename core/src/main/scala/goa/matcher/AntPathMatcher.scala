package goa.matcher

import java.util.StringTokenizer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.regex.Pattern

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

private[goa] class AntPathMatcher extends PathMatcher {
  val DEFAULT_PATH_SEPARATOR = "/"

  private val CACHE_TURNOFF_THRESHOLD = 65536

  private val VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?\\}")

  private val WILDCARD_CHARS = Array('*', '?', '{')

  private val PathSeparator = "/"

  private var pathSeparatorPatternCache: PathSeparatorPatternCache = new PathSeparatorPatternCache(DEFAULT_PATH_SEPARATOR)

  private var caseSensitive = true

  private var trimTokens = false

  private val CachePatterns: AtomicBoolean = new AtomicBoolean(true)

  private val TokenizedPatternCache = new ConcurrentHashMap[String, Array[String]](256).asScala

  private val StringMatcherCache = new ConcurrentHashMap[String, AntPathStringMatcher](256).asScala

  def tryMatch(pattern: String, path: String): Boolean = {
    doMatch(pattern, path, fullMatch = true, null)
  }

  def getPatternComparator(path: String) = new AntPatternComparator(path)

  def extractPathWithinPattern(pattern: String, path: String): String = {
    val patternParts = tokenizeToStringArray(pattern, this.PathSeparator, trimTokens, ignoreEmptyTokens = true)
    val pathParts = tokenizeToStringArray(path, this.PathSeparator, trimTokens, ignoreEmptyTokens = true)
    val builder = new StringBuilder
    var pathStarted = false
    var segment = 0

    while (segment < patternParts.length) {
      val patternPart = patternParts(segment)
      if (patternPart.contains('*') || patternPart.contains('?')) {
        while (segment < pathParts.length) {
          if (pathStarted || (segment == 0 && !pattern.startsWith(PathSeparator))) {
            builder.append(PathSeparator)
          }
          builder.append(pathParts(segment))
          pathStarted = true
          segment += 1
        }
      }
      segment += 1
    }

    builder.toString
  }

  def matchStart(pattern: String, path: String): Boolean = doMatch(pattern, path, fullMatch = false, null)

  private def doMatch(pattern: String, path: String, fullMatch: Boolean, uriTemplateVariables: mutable.HashMap[String, String]): Boolean = {
    if (path.startsWith(this.PathSeparator) != pattern.startsWith(this.PathSeparator)) {
      return false
    }

    val pattDirs = tokenizePattern(pattern)
    if (fullMatch && this.caseSensitive && !isPotentialMatch(path, pattDirs)) {
      return false
    }

    val pathDirs = tokenizePath(path)
    var patternIdxStart = 0
    var patternIdxEnd = pattDirs.length - 1
    var pathIdxStart = 0
    var pathIdxEnd = pathDirs.length - 1

    var b = false

    while (pathIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd && !b) {
      val pattDir = pattDirs(patternIdxStart)
      if ("**" == pattDir) {
        b = true
      } else {
        if (!matchStrings(pattDir, pathDirs(pathIdxStart), uriTemplateVariables)) {
          return false
        }
        patternIdxStart += 1
        pathIdxStart += 1
      }
    }

    if (pathIdxStart > pathIdxEnd) {
      if (patternIdxStart > patternIdxEnd) {
        return pattern.endsWith(this.PathSeparator) == path.endsWith(this.PathSeparator)
      }
      if (!fullMatch) {
        return true
      }
      if (patternIdxStart == patternIdxEnd && pattDirs(patternIdxStart).equals("*") && path.endsWith(this.PathSeparator)) {
        return true
      }
      var i = patternIdxStart
      while (i <= patternIdxEnd) {
        if (!(pattDirs(i) == "**")) {
          return false
        }
        i += 1
      }
      return true
    } else if (patternIdxStart > patternIdxEnd) {
      return false
    } else if (!fullMatch && "**" == pattDirs(patternIdxStart)) {
      return true
    }

    b = false
    while (patternIdxStart <= patternIdxEnd && pathIdxStart <= pathIdxEnd && !b) {
      val pattDir = pattDirs(patternIdxEnd)
      if (pattDir.equals("**")) {
        b = true
      } else {
        if (!matchStrings(pattDir, pathDirs(pathIdxEnd), uriTemplateVariables)) {
          return false
        }
        patternIdxEnd -= 1
        pathIdxEnd -= 1
      }

    }

    while (patternIdxStart != patternIdxEnd && pathIdxStart <= pathIdxEnd) {
      var patIdxTmp = -1
      var i = patternIdxStart + 1
      var b = false

      while (i <= patternIdxEnd && !b) {
        if (pattDirs(i) == "**") {
          patIdxTmp = i
          b = true
        } else {
          i += 1
        }
      }

      if (patIdxTmp == patternIdxStart + 1) {
        patternIdxStart += 1
      } else {
        var patLength = patIdxTmp - patternIdxStart - 1
        var strLength = pathIdxEnd - pathIdxStart + 1
        var foundIdx = -1

        def abc(i: Int): Boolean = {
          var j = 0
          while (j < patLength) {
            val subPat = pattDirs(patternIdxStart + j + 1)
            val subStr = pathDirs(pathIdxStart + i + j)
            if (!matchStrings(subPat, subStr, uriTemplateVariables)) {
              return false
            }
            j += 1
          }
          true
        }

        i = 0
        var bbb = false
        while (i < strLength - patLength && !bbb) {
          val b = abc(i)
          if (b) {
            foundIdx = pathIdxStart + i
            bbb = true
          } else {
            i += 1
          }
        }

        if (foundIdx == -1) {
          return false
        }
        patternIdxStart = patIdxTmp
        pathIdxStart = foundIdx + patLength
      }
    }

    for (i <- patternIdxStart until patternIdxEnd) {
      if (pattDirs(i) != "**") {
        return false
      }
    }
    true
  }

  private def matchStrings(pattern: String, str: String, uriTemplateVariables: mutable.HashMap[String, String]): Boolean = {
    getStringMatcher(pattern).matchStrings(str, uriTemplateVariables)
  }

  private def getStringMatcher(pattern: String): AntPathStringMatcher = {
    val cachePatterns = CachePatterns.get()
    if (cachePatterns) {
      StringMatcherCache.getOrElse(pattern, {
        val matcher = new AntPathStringMatcher(pattern, caseSensitive)
        if (cachePatterns && StringMatcherCache.size >= CACHE_TURNOFF_THRESHOLD) {
          deactivatePatternCache()
          return matcher
        } else if (cachePatterns) {
          StringMatcherCache.put(pattern, matcher)
        }
        matcher
      })
    } else new AntPathStringMatcher(pattern, caseSensitive)
  }

  private def tokenizePattern(pattern: String): Array[String] = {
    val cachePatterns = CachePatterns.get()
    if (cachePatterns) {
      TokenizedPatternCache.getOrElse(pattern, {
        val tokenized = tokenizePath(pattern)
        if (cachePatterns && TokenizedPatternCache.size >= CACHE_TURNOFF_THRESHOLD) {
          deactivatePatternCache()
        } else if (cachePatterns) {
          TokenizedPatternCache.put(pattern, tokenized)
        }
        tokenized
      })
    } else tokenizePath(pattern)
  }

  private def tokenizePath(path: String): Array[String] = {
    tokenizeToStringArray(path, this.PathSeparator, this.trimTokens, ignoreEmptyTokens = true)
  }

  private def concat(path1: String, path2: String) = {
    val path1EndsWithSeparator = path1.endsWith(this.PathSeparator)
    val path2StartsWithSeparator = path2.startsWith(this.PathSeparator)
    if (path1EndsWithSeparator && path2StartsWithSeparator) path1 + path2.substring(1)
    else if (path1EndsWithSeparator || path2StartsWithSeparator) path1 + path2
    else path1 + this.PathSeparator + path2
  }

  def combine(pattern1: String, pattern2: String): String = {
    if (!hasText(pattern1) && !hasText(pattern2)) return ""
    if (!hasText(pattern1)) return pattern2
    if (!hasText(pattern2)) return pattern1
    val pattern1ContainsUriVar = pattern1.indexOf('{') != -1
    if (!(pattern1 == pattern2) && !pattern1ContainsUriVar && tryMatch(pattern1, pattern2)) { // /* + /hotel -> /hotel ; "/*.*" + "/*.html" -> /*.html
      // However /user + /user -> /usr/user ; /{foo} + /bar -> /{foo}/bar
      return pattern2
    }
    // /hotels/* + /booking -> /hotels/booking
    // /hotels/* + booking -> /hotels/booking
    if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnWildCard)) return concat(pattern1.substring(0, pattern1.length - 2), pattern2)
    // /hotels/** + /booking -> /hotels/**/booking
    // /hotels/** + booking -> /hotels/**/booking
    if (pattern1.endsWith(this.pathSeparatorPatternCache.getEndsOnDoubleWildCard)) return concat(pattern1, pattern2)
    val starDotPos1 = pattern1.indexOf("*.")
    if (pattern1ContainsUriVar || starDotPos1 == -1 || this.PathSeparator == ".") { // simply concatenate the two patterns
      return concat(pattern1, pattern2)
    }
    val ext1 = pattern1.substring(starDotPos1 + 1)
    val dotPos2 = pattern2.indexOf('.')
    val file2 = if (dotPos2 == -1) {
      pattern2
    }
    else {
      pattern2.substring(0, dotPos2)
    }
    val ext2 = if (dotPos2 == -1) {
      ""
    }
    else {
      pattern2.substring(dotPos2)
    }
    val ext1All = ext1 == ".*" || ext1 == ""
    val ext2All = ext2 == ".*" || ext2 == ""
    if (!ext1All && !ext2All) throw new IllegalArgumentException("Cannot combine patterns: " + pattern1 + " vs " + pattern2)
    val ext = if (ext1All) {
      ext2
    }
    else {
      ext1
    }
    file2 + ext
  }

  def extractUriTemplateVariables(pattern: String, path: String): mutable.HashMap[String, String] = {
    val variables = mutable.HashMap[String, String]()
    val result: Boolean = doMatch(pattern, path, fullMatch = true, variables)
    if (!result) throw new IllegalStateException("Pattern \"" + pattern + "\" is not a match for \"" + path + "\"")
    variables
  }

  private def hasText(str: String): Boolean = str != null && str.nonEmpty && containsText(str)

  private def containsText(str: CharSequence): Boolean = {
    str.toString.trim.length > 0
  }

  private def tokenizeToStringArray(str: String, delimiters: String, trimTokens: Boolean, ignoreEmptyTokens: Boolean) = {
    if (str == null) {
      Array.empty[String]
    } else {
      val st = new StringTokenizer(str, delimiters)
      val tokens = ArrayBuffer[String]()
      while (st.hasMoreTokens) {
        var token = st.nextToken()
        token = if (trimTokens) token.trim else token
        if (!ignoreEmptyTokens || token.length > 0) {
          tokens += token
        }
      }
      tokens.toArray
    }
  }

  private def deactivatePatternCache(): Unit = {
    CachePatterns.set(false)
    TokenizedPatternCache.clear()
    StringMatcherCache.clear()
  }

  private def isPotentialMatch(path: String, pattDirs: Array[String]): Boolean = {
    if (!this.trimTokens) {
      var pos = 0
      for (pattDir <- pattDirs) {
        var skipped = skipSeparator(path, pos, PathSeparator)
        pos += skipped
        skipped = skipSegment(path, pos, pattDir)
        if (skipped < pattDir.length) {
          return skipped > 0 || (pattDir.length > 0 && isWildcardChar(pattDir.charAt(0)))
        }
        pos += skipped
      }
    }
    true
  }

  private def skipSeparator(path: String, pos: Int, separator: String): Int = {
    var skipped = 0
    while (path.startsWith(separator, pos + skipped)) {
      skipped += separator.length
    }
    skipped
  }

  private def skipSegment(path: String, pos: Int, prefix: String): Int = {
    var skipped = 0
    var i = 0
    while (i < prefix.length) {
      val c = prefix.charAt(i)
      if (isWildcardChar(c)) {
        return skipped
      }
      val currPos = pos + skipped
      if (currPos >= path.length) {
        return 0
      }
      if (c == path.charAt(currPos)) {
        skipped += 1
      }
      i += 1
    }
    skipped
  }

  private def isWildcardChar(c: Char): Boolean = {
    WILDCARD_CHARS.contains(c)
  }
}

private[matcher] class AntPathStringMatcher {

  private val GLOB_PATTERN = Pattern.compile("\\?|\\*|\\{((?:\\{[^/]+?\\}|[^/{}]|\\\\[{}])+?)\\}")

  private val DEFAULT_VARIABLE_PATTERN = "(.*)"

  private var pattern: Pattern = _

  private val variableNames = ArrayBuffer[String]()

  def this(pattern: String, caseSensitive: Boolean) {
    this()
    val patternBuilder = new StringBuilder()
    val matcher = GLOB_PATTERN.matcher(pattern)
    var end = 0
    while (matcher.find()) {
      patternBuilder.append(quote(pattern, end, matcher.start()))
      val _match = matcher.group()
      if ("?" == _match) {
        patternBuilder.append(".")
      } else if ("*" == _match) {
        patternBuilder.append(".*")
      } else if (_match.startsWith("{") && _match.endsWith("}")) {
        var colonIdx = _match.indexOf(":")
        if (colonIdx == -1) {
          patternBuilder.append(DEFAULT_VARIABLE_PATTERN)
          this.variableNames += matcher.group(1)
        } else {
          val variablePattern = _match.substring(colonIdx + 1, _match.length - 1)
          patternBuilder.append('(')
          patternBuilder.append(variablePattern)
          patternBuilder.append(')')
          val variableName = _match.substring(1, colonIdx)
          this.variableNames += variableName
        }
      }
      end = matcher.end()
    }
    patternBuilder.append(quote(pattern, end, pattern.length))
    this.pattern = if (caseSensitive) {
      Pattern.compile(patternBuilder.toString)
    } else {
      Pattern.compile(patternBuilder.toString, Pattern.CASE_INSENSITIVE)
    }

  }

  def this(pattern: String) {
    this(pattern, true)
  }

  private def quote(s: String, start: Int, end: Int): String = {
    if (start == end) {
      ""
    } else Pattern.quote(s.substring(start, end))
  }

  def matchStrings(str: String, uriTemplateVariables: mutable.HashMap[String, String]): Boolean = {
    val matcher = pattern.matcher(str)
    if (matcher.matches()) {
      if (uriTemplateVariables != null) {
        if (this.variableNames.size != matcher.groupCount()) {
          throw new IllegalArgumentException
        }
        var i = 1
        while (i <= matcher.groupCount()) {
          val name = variableNames(i - 1)
          val value = matcher.group(i)
          uriTemplateVariables(name) = value
          i += 1
        }
      }
      true
    } else false
  }
}

private[matcher] class AntPatternComparator(path: String) extends Ordering[String] {
  def compare(pattern1: String, pattern2: String): Int = {
    val info1 = new PatternInfo(pattern1)
    val info2 = new PatternInfo(pattern2)

    if (info1.isLeastSpecific && info2.isLeastSpecific) return 0
    else if (info1.isLeastSpecific) return 1
    else if (info2.isLeastSpecific) return -1

    val pattern1EqualsPath = pattern1 == path
    val pattern2EqualsPath = pattern2 == path
    if (pattern1EqualsPath && pattern2EqualsPath) return 0
    else if (pattern1EqualsPath) return -1
    else if (pattern2EqualsPath) return 1

    if (info1.isPrefixPattern && info2.getDoubleWildcards == 0) return 1
    else if (info2.isPrefixPattern && info1.getDoubleWildcards == 0) return -1

    if (info1.getTotalCount != info2.getTotalCount) return info1.getTotalCount - info2.getTotalCount

    if (info1.getLength != info2.getLength) return info2.getLength - info1.getLength

    if (info1.getSingleWildcards < info2.getSingleWildcards) return -1
    else if (info2.getSingleWildcards < info1.getSingleWildcards) return 1

    if (info1.getUriVars < info2.getUriVars) return -1
    else if (info2.getUriVars < info1.getUriVars) return 1

    0

  }
}

private[matcher] class PathSeparatorPatternCache(pathSeparator: String) {

  private val endsOnWildCard: String = pathSeparator + "*"

  private val endsOnDoubleWildCard: String = pathSeparator + "**"

  def getEndsOnWildCard: String = this.endsOnWildCard

  def getEndsOnDoubleWildCard: String = this.endsOnDoubleWildCard

}

private[matcher] class PatternInfo {

  private var pattern: String = _

  private val VARIABLE_PATTERN = Pattern.compile("\\{[^/]+?\\}")

  private var uriVars = 0

  private var singleWildcards = 0

  private var doubleWildcards = 0

  private var catchAllPattern = false

  private var prefixPattern = false

  private var length = -100

  def this(pattern: String) {
    this()
    this.pattern = pattern
    if (this.pattern != null) {
      initCounters()
      this.catchAllPattern = this.pattern == "/**"
      this.prefixPattern = !this.catchAllPattern && this.pattern.endsWith("/**")
    }
    if (this.uriVars == 0) {
      this.length = if (this.pattern != null) this.pattern.length else 0
    }
  }

  private def initCounters(): Unit = {
    var pos = 0
    if (this.pattern != null) {
      while (pos < this.pattern.length()) {
        if (this.pattern.charAt(pos) == '{') {
          this.uriVars += 1
          pos += 1
        } else if (this.pattern.charAt(pos) == '*') {
          if (pos + 1 < this.pattern.length() && this.pattern.charAt(pos + 1) == '*') {
            this.doubleWildcards += 1
            pos += 2
          } else if (pos > 0 && !this.pattern.substring(pos - 1).equals(".*")) {
            this.singleWildcards += 1
            pos += 1
          } else {
            pos += 1
          }
        } else {
          pos += 1
        }
      }
    }
  }

  def getUriVars: Int = this.uriVars

  def getSingleWildcards: Int = this.singleWildcards

  def getDoubleWildcards: Int = this.doubleWildcards

  def isLeastSpecific: Boolean = this.pattern == null || this.catchAllPattern

  def isPrefixPattern: Boolean = this.prefixPattern

  def getTotalCount: Int = this.uriVars + this.singleWildcards + (2 * this.doubleWildcards)

  def getLength: Int = {

    if (this.length == -100) {
      this.length = if (this.pattern != null) {
        VARIABLE_PATTERN.matcher(this.pattern).replaceAll("#").length
      } else 0
    }
    this.length
  }
}