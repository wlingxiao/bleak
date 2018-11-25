package bleak.matcher

import java.util

import bleak.BaseTests
import org.junit.Assert._
import org.scalatest.{FunSuite, Matchers}

class AntPathMatcherTest extends BaseTests {

  val pathMatcher = new AntPathMatcher

  def assertTrue(b: Boolean): Unit = {
    b should be(true)
  }

  def assertFalse(b: Boolean): Unit = {
    b should be(false)
  }

  test("全匹配") {

    // test exact matching// test exact matching

    assertTrue(pathMatcher.tryMatch("test", "test"))
    assertTrue(pathMatcher.tryMatch("/test", "/test"))
    assertTrue(pathMatcher.tryMatch("http://example.org", "http://example.org")) // SPR-14141

    assertFalse(pathMatcher.tryMatch("/test.jpg", "test.jpg"))
    assertFalse(pathMatcher.tryMatch("test", "/test"))
    assertFalse(pathMatcher.tryMatch("/test", "test"))

    // test matching with ?'s// test matching with ?'s

    assertTrue(pathMatcher.tryMatch("t?st", "test"))
    assertTrue(pathMatcher.tryMatch("??st", "test"))
    assertTrue(pathMatcher.tryMatch("tes?", "test"))
    assertTrue(pathMatcher.tryMatch("te??", "test"))
    assertTrue(pathMatcher.tryMatch("?es?", "test"))
    assertFalse(pathMatcher.tryMatch("tes?", "tes"))
    assertFalse(pathMatcher.tryMatch("tes?", "testt"))
    assertFalse(pathMatcher.tryMatch("tes?", "tsst"))

    // test matching with *'s// test matching with *'s

    assertTrue(pathMatcher.tryMatch("*", "test"))
    assertTrue(pathMatcher.tryMatch("test*", "test"))
    assertTrue(pathMatcher.tryMatch("test*", "testTest"))
    assertTrue(pathMatcher.tryMatch("test/*", "test/Test"))
    assertTrue(pathMatcher.tryMatch("test/*", "test/t"))
    assertTrue(pathMatcher.tryMatch("test/*", "test/"))
    assertTrue(pathMatcher.tryMatch("*test*", "AnothertestTest"))
    assertTrue(pathMatcher.tryMatch("*test", "Anothertest"))
    assertTrue(pathMatcher.tryMatch("*.*", "test."))
    assertTrue(pathMatcher.tryMatch("*.*", "test.test"))
    assertTrue(pathMatcher.tryMatch("*.*", "test.test.test"))
    assertTrue(pathMatcher.tryMatch("test*aaa", "testblaaaa"))
    assertFalse(pathMatcher.tryMatch("test*", "tst"))
    assertFalse(pathMatcher.tryMatch("test*", "tsttest"))
    assertFalse(pathMatcher.tryMatch("test*", "test/"))
    assertFalse(pathMatcher.tryMatch("test*", "test/t"))
    assertFalse(pathMatcher.tryMatch("test/*", "test"))
    assertFalse(pathMatcher.tryMatch("*test*", "tsttst"))
    assertFalse(pathMatcher.tryMatch("*test", "tsttst"))
    assertFalse(pathMatcher.tryMatch("*.*", "tsttst"))
    assertFalse(pathMatcher.tryMatch("test*aaa", "test"))
    assertFalse(pathMatcher.tryMatch("test*aaa", "testblaaab"))

    // test matching with ?'s and /'s// test matching with ?'s and /'s

    assertTrue(pathMatcher.tryMatch("/?", "/a"))
    assertTrue(pathMatcher.tryMatch("/?/a", "/a/a"))
    assertTrue(pathMatcher.tryMatch("/a/?", "/a/b"))
    assertTrue(pathMatcher.tryMatch("/??/a", "/aa/a"))
    assertTrue(pathMatcher.tryMatch("/a/??", "/a/bb"))
    assertTrue(pathMatcher.tryMatch("/?", "/a"))


    // test matching with **'s// test matching with **'s

    assertTrue(pathMatcher.tryMatch("/**", "/testing/testing"))
    assertTrue(pathMatcher.tryMatch("/*/**", "/testing/testing"))
    assertTrue(pathMatcher.tryMatch("/**/*", "/testing/testing"))
    assertTrue(pathMatcher.tryMatch("/bla/**/bla", "/bla/testing/testing/bla"))
    assertTrue(pathMatcher.tryMatch("/bla/**/bla", "/bla/testing/testing/bla/bla"))
    assertTrue(pathMatcher.tryMatch("/**/test", "/bla/bla/test"))
    assertTrue(pathMatcher.tryMatch("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla"))
    assertTrue(pathMatcher.tryMatch("/bla*bla/test", "/blaXXXbla/test"))
    assertTrue(pathMatcher.tryMatch("/*bla/test", "/XXXbla/test"))
    assertFalse(pathMatcher.tryMatch("/bla*bla/test", "/blaXXXbl/test"))
    assertFalse(pathMatcher.tryMatch("/*bla/test", "XXXblab/test"))
    assertFalse(pathMatcher.tryMatch("/*bla/test", "XXXbl/test"))

    assertFalse(pathMatcher.tryMatch("/????", "/bala/bla"))
    assertFalse(pathMatcher.tryMatch("/**/*bla", "/bla/bla/bla/bbb"))

    assertTrue(pathMatcher.tryMatch("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/"))
    assertTrue(pathMatcher.tryMatch("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing"))
    assertTrue(pathMatcher.tryMatch("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing"))
    assertTrue(pathMatcher.tryMatch("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"))

    assertTrue(pathMatcher.tryMatch("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing/"))
    assertTrue(pathMatcher.tryMatch("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing"))
    assertTrue(pathMatcher.tryMatch("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing"))
    assertFalse(pathMatcher.tryMatch("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing/testing"))

    assertFalse(pathMatcher.tryMatch("/x/x/**/bla", "/x/x/x/"))

    assertTrue(pathMatcher.tryMatch("/foo/bar/**", "/foo/bar"))

    assertTrue(pathMatcher.tryMatch("", ""))

    assertTrue(pathMatcher.tryMatch("/{bla}.*", "/testing.html"))
  }

  test("withMatchStart") {

    // test exact matching// test exact matching

    assertTrue(pathMatcher.matchStart("test", "test"))
    assertTrue(pathMatcher.matchStart("/test", "/test"))
    assertFalse(pathMatcher.matchStart("/test.jpg", "test.jpg"))
    assertFalse(pathMatcher.matchStart("test", "/test"))
    assertFalse(pathMatcher.matchStart("/test", "test"))

    // test matching with ?'s// test matching with ?'s

    assertTrue(pathMatcher.matchStart("t?st", "test"))
    assertTrue(pathMatcher.matchStart("??st", "test"))
    assertTrue(pathMatcher.matchStart("tes?", "test"))
    assertTrue(pathMatcher.matchStart("te??", "test"))
    assertTrue(pathMatcher.matchStart("?es?", "test"))
    assertFalse(pathMatcher.matchStart("tes?", "tes"))
    assertFalse(pathMatcher.matchStart("tes?", "testt"))
    assertFalse(pathMatcher.matchStart("tes?", "tsst"))

    // test matching with *'s// test matching with *'s

    assertTrue(pathMatcher.matchStart("*", "test"))
    assertTrue(pathMatcher.matchStart("test*", "test"))
    assertTrue(pathMatcher.matchStart("test*", "testTest"))
    assertTrue(pathMatcher.matchStart("test/*", "test/Test"))
    assertTrue(pathMatcher.matchStart("test/*", "test/t"))
    assertTrue(pathMatcher.matchStart("test/*", "test/"))
    assertTrue(pathMatcher.matchStart("*test*", "AnothertestTest"))
    assertTrue(pathMatcher.matchStart("*test", "Anothertest"))
    assertTrue(pathMatcher.matchStart("*.*", "test."))
    assertTrue(pathMatcher.matchStart("*.*", "test.test"))
    assertTrue(pathMatcher.matchStart("*.*", "test.test.test"))
    assertTrue(pathMatcher.matchStart("test*aaa", "testblaaaa"))
    assertFalse(pathMatcher.matchStart("test*", "tst"))
    assertFalse(pathMatcher.matchStart("test*", "test/"))
    assertFalse(pathMatcher.matchStart("test*", "tsttest"))
    assertFalse(pathMatcher.matchStart("test*", "test/"))
    assertFalse(pathMatcher.matchStart("test*", "test/t"))
    assertTrue(pathMatcher.matchStart("test/*", "test"))
    assertTrue(pathMatcher.matchStart("test/t*.txt", "test"))
    assertFalse(pathMatcher.matchStart("*test*", "tsttst"))
    assertFalse(pathMatcher.matchStart("*test", "tsttst"))
    assertFalse(pathMatcher.matchStart("*.*", "tsttst"))
    assertFalse(pathMatcher.matchStart("test*aaa", "test"))
    assertFalse(pathMatcher.matchStart("test*aaa", "testblaaab"))

    // test matching with ?'s and /'s// test matching with ?'s and /'s

    assertTrue(pathMatcher.matchStart("/?", "/a"))
    assertTrue(pathMatcher.matchStart("/?/a", "/a/a"))
    assertTrue(pathMatcher.matchStart("/a/?", "/a/b"))
    assertTrue(pathMatcher.matchStart("/??/a", "/aa/a"))
    assertTrue(pathMatcher.matchStart("/a/??", "/a/bb"))
    assertTrue(pathMatcher.matchStart("/?", "/a"))

    // test matching with **'s// test matching with **'s

    assertTrue(pathMatcher.matchStart("/**", "/testing/testing"))
    assertTrue(pathMatcher.matchStart("/*/**", "/testing/testing"))
    assertTrue(pathMatcher.matchStart("/**/*", "/testing/testing"))
    assertTrue(pathMatcher.matchStart("test*/**", "test/"))
    assertTrue(pathMatcher.matchStart("test*/**", "test/t"))
    assertTrue(pathMatcher.matchStart("/bla/**/bla", "/bla/testing/testing/bla"))
    assertTrue(pathMatcher.matchStart("/bla/**/bla", "/bla/testing/testing/bla/bla"))
    assertTrue(pathMatcher.matchStart("/**/test", "/bla/bla/test"))
    assertTrue(pathMatcher.matchStart("/bla/**/**/bla", "/bla/bla/bla/bla/bla/bla"))
    assertTrue(pathMatcher.matchStart("/bla*bla/test", "/blaXXXbla/test"))
    assertTrue(pathMatcher.matchStart("/*bla/test", "/XXXbla/test"))
    assertFalse(pathMatcher.matchStart("/bla*bla/test", "/blaXXXbl/test"))
    assertFalse(pathMatcher.matchStart("/*bla/test", "XXXblab/test"))
    assertFalse(pathMatcher.matchStart("/*bla/test", "XXXbl/test"))

    assertFalse(pathMatcher.matchStart("/????", "/bala/bla"))
    assertTrue(pathMatcher.matchStart("/**/*bla", "/bla/bla/bla/bbb"))

    assertTrue(pathMatcher.matchStart("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing/"))
    assertTrue(pathMatcher.matchStart("/*bla*/**/bla/*", "/XXXblaXXXX/testing/testing/bla/testing"))
    assertTrue(pathMatcher.matchStart("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing"))
    assertTrue(pathMatcher.matchStart("/*bla*/**/bla/**", "/XXXblaXXXX/testing/testing/bla/testing/testing.jpg"))

    assertTrue(pathMatcher.matchStart("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing/"))
    assertTrue(pathMatcher.matchStart("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing"))
    assertTrue(pathMatcher.matchStart("*bla*/**/bla/**", "XXXblaXXXX/testing/testing/bla/testing/testing"))
    assertTrue(pathMatcher.matchStart("*bla*/**/bla/*", "XXXblaXXXX/testing/testing/bla/testing/testing"))

    assertTrue(pathMatcher.matchStart("/x/x/**/bla", "/x/x/x/"))

    assertTrue(pathMatcher.matchStart("", ""))

  }

  test("extractPathWithinPattern") {
    assertEquals("", pathMatcher.extractPathWithinPattern("/docs/commit.html", "/docs/commit.html"))

    assertEquals("cvs/commit", pathMatcher.extractPathWithinPattern("/docs/*", "/docs/cvs/commit"))
    assertEquals("commit.html", pathMatcher.extractPathWithinPattern("/docs/cvs/*.html", "/docs/cvs/commit.html"))
    assertEquals("cvs/commit", pathMatcher.extractPathWithinPattern("/docs/**", "/docs/cvs/commit"))
    assertEquals("cvs/commit.html", pathMatcher.extractPathWithinPattern("/docs/**/*.html", "/docs/cvs/commit.html"))
    assertEquals("commit.html", pathMatcher.extractPathWithinPattern("/docs/**/*.html", "/docs/commit.html"))
    assertEquals("commit.html", pathMatcher.extractPathWithinPattern("/*.html", "/commit.html"))
    assertEquals("docs/commit.html", pathMatcher.extractPathWithinPattern("/*.html", "/docs/commit.html"))
    assertEquals("/commit.html", pathMatcher.extractPathWithinPattern("*.html", "/commit.html"))
    assertEquals("/docs/commit.html", pathMatcher.extractPathWithinPattern("*.html", "/docs/commit.html"))
    assertEquals("/docs/commit.html", pathMatcher.extractPathWithinPattern("**/*.*", "/docs/commit.html"))
    assertEquals("/docs/commit.html", pathMatcher.extractPathWithinPattern("*", "/docs/commit.html"))
    // SPR-10515
    assertEquals("/docs/cvs/other/commit.html", pathMatcher.extractPathWithinPattern("**/commit.html", "/docs/cvs/other/commit.html"))
    assertEquals("cvs/other/commit.html", pathMatcher.extractPathWithinPattern("/docs/**/commit.html", "/docs/cvs/other/commit.html"))
    assertEquals("cvs/other/commit.html", pathMatcher.extractPathWithinPattern("/docs/**/**/**/**", "/docs/cvs/other/commit.html"))

    assertEquals("docs/cvs/commit", pathMatcher.extractPathWithinPattern("/d?cs/*", "/docs/cvs/commit"))
    assertEquals("cvs/commit.html", pathMatcher.extractPathWithinPattern("/docs/c?s/*.html", "/docs/cvs/commit.html"))
    assertEquals("docs/cvs/commit", pathMatcher.extractPathWithinPattern("/d?cs/**", "/docs/cvs/commit"))
    assertEquals("docs/cvs/commit.html", pathMatcher.extractPathWithinPattern("/d?cs/**/*.html", "/docs/cvs/commit.html"))
  }

  /*def assertEquals(str: String, str1: String): Unit = {
    str should equal(str1)
  }

  def assertEquals(str: Int, str1: Int): Unit = {
    str1 should equal(str)
  }*/

  test("patternComparator") {
    val comparator = pathMatcher.getPatternComparator("/hotels/new")

    assertEquals(0, comparator.compare(null, null))
    assertEquals(1, comparator.compare(null, "/hotels/new"))
    assertEquals(-1, comparator.compare("/hotels/new", null))

    assertEquals(0, comparator.compare("/hotels/new", "/hotels/new"))

    assertEquals(-1, comparator.compare("/hotels/new", "/hotels/*"))
    assertEquals(1, comparator.compare("/hotels/*", "/hotels/new"))
    assertEquals(0, comparator.compare("/hotels/*", "/hotels/*"))

    assertEquals(-1, comparator.compare("/hotels/new", "/hotels/{hotel}"))
    assertEquals(1, comparator.compare("/hotels/{hotel}", "/hotels/new"))
    assertEquals(0, comparator.compare("/hotels/{hotel}", "/hotels/{hotel}"))
    assertEquals(-1, comparator.compare("/hotels/{hotel}/booking", "/hotels/{hotel}/bookings/{booking}"))
    assertEquals(1, comparator.compare("/hotels/{hotel}/bookings/{booking}", "/hotels/{hotel}/booking"))

    // SPR-10550
    assertEquals(-1, comparator.compare("/hotels/{hotel}/bookings/{booking}/cutomers/{customer}", "/**"))
    assertEquals(1, comparator.compare("/**", "/hotels/{hotel}/bookings/{booking}/cutomers/{customer}"))
    assertEquals(0, comparator.compare("/**", "/**"))

    assertEquals(-1, comparator.compare("/hotels/{hotel}", "/hotels/*"))
    assertEquals(1, comparator.compare("/hotels/*", "/hotels/{hotel}"))

    assertEquals(-1, comparator.compare("/hotels/*", "/hotels/*/**"))
    assertEquals(1, comparator.compare("/hotels/*/**", "/hotels/*"))

    assertEquals(-1, comparator.compare("/hotels/new", "/hotels/new.*"))
    assertEquals(2, comparator.compare("/hotels/{hotel}", "/hotels/{hotel}.*"))

    // SPR-6741
    assertEquals(-1, comparator.compare("/hotels/{hotel}/bookings/{booking}/cutomers/{customer}", "/hotels/**"))
    assertEquals(1, comparator.compare("/hotels/**", "/hotels/{hotel}/bookings/{booking}/cutomers/{customer}"))
    assertEquals(1, comparator.compare("/hotels/foo/bar/**", "/hotels/{hotel}"))
    assertEquals(-1, comparator.compare("/hotels/{hotel}", "/hotels/foo/bar/**"))
    assertEquals(2, comparator.compare("/hotels/**/bookings/**", "/hotels/**"))
    assertEquals(-2, comparator.compare("/hotels/**", "/hotels/**/bookings/**"))

    // SPR-8683
    assertEquals(1, comparator.compare("/**", "/hotels/{hotel}"))

    // longer is better
    assertEquals(1, comparator.compare("/hotels", "/hotels2"))

    // SPR-13139
    assertEquals(-1, comparator.compare("*", "*/**"))
    assertEquals(1, comparator.compare("*/**", "*"))
  }

  test("patternComparatorSort") {
    var comparator = pathMatcher.getPatternComparator("/hotels/new")

    import java.util
    import java.util.Collections
    val paths = new util.ArrayList[String](3)

    paths.add(null)
    paths.add("/hotels/new")
    Collections.sort(paths, comparator)
    assertEquals("/hotels/new", paths.get(0))
    assertNull(paths.get(1))
    paths.clear()

    paths.add("/hotels/new")
    paths.add(null)
    Collections.sort(paths, comparator)
    assertEquals("/hotels/new", paths.get(0))
    assertNull(paths.get(1))
    paths.clear()

    paths.add("/hotels/*")
    paths.add("/hotels/new")
    Collections.sort(paths, comparator)
    assertEquals("/hotels/new", paths.get(0))
    assertEquals("/hotels/*", paths.get(1))
    paths.clear()

    paths.add("/hotels/new")
    paths.add("/hotels/*")
    Collections.sort(paths, comparator)
    assertEquals("/hotels/new", paths.get(0))
    assertEquals("/hotels/*", paths.get(1))
    paths.clear()

    paths.add("/hotels/**")
    paths.add("/hotels/*")
    Collections.sort(paths, comparator)
    assertEquals("/hotels/*", paths.get(0))
    assertEquals("/hotels/**", paths.get(1))
    paths.clear()

    paths.add("/hotels/*")
    paths.add("/hotels/**")
    Collections.sort(paths, comparator)
    assertEquals("/hotels/*", paths.get(0))
    assertEquals("/hotels/**", paths.get(1))
    paths.clear()

    paths.add("/hotels/{hotel}")
    paths.add("/hotels/new")
    Collections.sort(paths, comparator)
    assertEquals("/hotels/new", paths.get(0))
    assertEquals("/hotels/{hotel}", paths.get(1))
    paths.clear()

    paths.add("/hotels/new")
    paths.add("/hotels/{hotel}")
    Collections.sort(paths, comparator)
    assertEquals("/hotels/new", paths.get(0))
    assertEquals("/hotels/{hotel}", paths.get(1))
    paths.clear()

    paths.add("/hotels/*")
    paths.add("/hotels/{hotel}")
    paths.add("/hotels/new")
    Collections.sort(paths, comparator)
    assertEquals("/hotels/new", paths.get(0))
    assertEquals("/hotels/{hotel}", paths.get(1))
    assertEquals("/hotels/*", paths.get(2))
    paths.clear()

    paths.add("/hotels/ne*")
    paths.add("/hotels/n*")
    Collections.shuffle(paths)
    Collections.sort(paths, comparator)
    assertEquals("/hotels/ne*", paths.get(0))
    assertEquals("/hotels/n*", paths.get(1))
    paths.clear()

    comparator = pathMatcher.getPatternComparator("/hotels/new.html")
    paths.add("/hotels/new.*")
    paths.add("/hotels/{hotel}")
    Collections.shuffle(paths)
    Collections.sort(paths, comparator)
    assertEquals("/hotels/new.*", paths.get(0))
    assertEquals("/hotels/{hotel}", paths.get(1))
    paths.clear()

    comparator = pathMatcher.getPatternComparator("/web/endUser/action/login.html")
    paths.add("/**/login.*")
    paths.add("/**/endUser/action/login.*")
    Collections.sort(paths, comparator)
    assertEquals("/**/endUser/action/login.*", paths.get(0))
    assertEquals("/**/login.*", paths.get(1))


  }

  test("combine") {
    assertEquals("", pathMatcher.combine(null, null))
    assertEquals("/hotels", pathMatcher.combine("/hotels", null))
    assertEquals("/hotels", pathMatcher.combine(null, "/hotels"))
    assertEquals("/hotels/booking", pathMatcher.combine("/hotels/*", "booking"))
    assertEquals("/hotels/booking", pathMatcher.combine("/hotels/*", "/booking"))
    assertEquals("/hotels/**/booking", pathMatcher.combine("/hotels/**", "booking"))
    assertEquals("/hotels/**/booking", pathMatcher.combine("/hotels/**", "/booking"))
    assertEquals("/hotels/booking", pathMatcher.combine("/hotels", "/booking"))
    assertEquals("/hotels/booking", pathMatcher.combine("/hotels", "booking"))
    assertEquals("/hotels/booking", pathMatcher.combine("/hotels/", "booking"))
    assertEquals("/hotels/{hotel}", pathMatcher.combine("/hotels/*", "{hotel}"))
    assertEquals("/hotels/**/{hotel}", pathMatcher.combine("/hotels/**", "{hotel}"))
    assertEquals("/hotels/{hotel}", pathMatcher.combine("/hotels", "{hotel}"))
    assertEquals("/hotels/{hotel}.*", pathMatcher.combine("/hotels", "{hotel}.*"))
    assertEquals("/hotels/*/booking/{booking}", pathMatcher.combine("/hotels/*/booking", "{booking}"))
    assertEquals("/hotel.html", pathMatcher.combine("/*.html", "/hotel.html"))
    assertEquals("/hotel.html", pathMatcher.combine("/*.html", "/hotel"))
    assertEquals("/hotel.html", pathMatcher.combine("/*.html", "/hotel.*"))
    assertEquals("/*.html", pathMatcher.combine("/**", "/*.html"))
    assertEquals("/*.html", pathMatcher.combine("/*", "/*.html"))
    assertEquals("/*.html", pathMatcher.combine("/*.*", "/*.html"))
    assertEquals("/{foo}/bar", pathMatcher.combine("/{foo}", "/bar")) // SPR-8858

    assertEquals("/user/user", pathMatcher.combine("/user", "/user")) // SPR-7970

    assertEquals("/{foo:.*[^0-9].*}/edit/", pathMatcher.combine("/{foo:.*[^0-9].*}", "/edit/")) // SPR-10062

    assertEquals("/1.0/foo/test", pathMatcher.combine("/1.0", "/foo/test")) // SPR-10554

    assertEquals("/hotel", pathMatcher.combine("/", "/hotel")) // SPR-12975

    assertEquals("/hotel/booking", pathMatcher.combine("/hotel/", "/booking"))
  }

  test("extractUriTemplateVariables") {
    import java.util.Collections

    import scala.collection.JavaConverters._
    var result = pathMatcher.extractUriTemplateVariables("/hotels/{hotel}", "/hotels/1").asJava
    assertEquals(Collections.singletonMap("hotel", "1"), result)

    result = pathMatcher.extractUriTemplateVariables("/h?tels/{hotel}", "/hotels/1").asJava
    assertEquals(Collections.singletonMap("hotel", "1"), result)

    result = pathMatcher.extractUriTemplateVariables("/hotels/{hotel}/bookings/{booking}", "/hotels/1/bookings/2").asJava
    var expected = new util.LinkedHashMap[String, String]()
    expected.put("hotel", "1")
    expected.put("booking", "2")
    assertEquals(expected, result)

    result = pathMatcher.extractUriTemplateVariables("/**/hotels/**/{hotel}", "/foo/hotels/bar/1").asJava
    assertEquals(Collections.singletonMap("hotel", "1"), result)

    result = pathMatcher.extractUriTemplateVariables("/{page}.html", "/42.html").asJava
    assertEquals(Collections.singletonMap("page", "42"), result)

    result = pathMatcher.extractUriTemplateVariables("/{page}.*", "/42.html").asJava
    assertEquals(Collections.singletonMap("page", "42"), result)

    result = pathMatcher.extractUriTemplateVariables("/A-{B}-C", "/A-b-C").asJava
    assertEquals(Collections.singletonMap("B", "b"), result)

    result = pathMatcher.extractUriTemplateVariables("/{name}.{extension}", "/test.html").asJava
    expected = new util.LinkedHashMap[String, String]()
    expected.put("name", "test")
    expected.put("extension", "html")
    assertEquals(expected, result)
  }

  test("extractUriTemplateVarsRegexQualifiers") {
    import scala.collection.JavaConverters._
    var result = pathMatcher.extractUriTemplateVariables("{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.]+}.jar", "com.example-sources-1.0.0.jar").asJava
    assertEquals("com.example", result.get("symbolicName"))
    assertEquals("1.0.0", result.get("version"))

    result = pathMatcher.extractUriTemplateVariables("{symbolicName:[\\w\\.]+}-sources-{version:[\\d\\.]+}-{year:\\d{4}}{month:\\d{2}}{day:\\d{2}}.jar", "com.example-sources-1.0.0-20100220.jar").asJava
    assertEquals("com.example", result.get("symbolicName"))
    assertEquals("1.0.0", result.get("version"))
    assertEquals("2010", result.get("year"))
    assertEquals("02", result.get("month"))
    assertEquals("20", result.get("day"))

    result = pathMatcher.extractUriTemplateVariables("{symbolicName:[\\p{L}\\.]+}-sources-{version:[\\p{N}\\.\\{\\}]+}.jar", "com.example-sources-1.0.0.{12}.jar").asJava
    assertEquals("com.example", result.get("symbolicName"))
    assertEquals("1.0.0.{12}", result.get("version"))
  }

  test("extractUriTemplateVariablesRegex") {
    import scala.collection.JavaConverters._
    var result = pathMatcher.extractUriTemplateVariables("{symbolicName:[\\w\\.]+}-{version:[\\w\\.]+}.jar", "com.example-1.0.0.jar").asJava
    assertEquals("com.example", result.get("symbolicName"))
    assertEquals("1.0.0", result.get("version"))

    result = pathMatcher.extractUriTemplateVariables("{symbolicName:[\\w\\.]+}-sources-{version:[\\w\\.]+}.jar", "com.example-sources-1.0.0.jar").asJava
    assertEquals("com.example", result.get("symbolicName"))
    assertEquals("1.0.0", result.get("version"))
  }

}
