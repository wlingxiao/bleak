package goa

import java.nio.ByteBuffer

import goa.http1.{BodyReader, HttpRequest}
import goa.util.BufferUtils

class CookiesTests extends BaseTests {

  test("test cookies operation") {
    val headers = Seq("Cookie" -> "abcdefg=1526474738; SessionId=1527771970; abcdefg=3666")
    val httpRequest = HttpRequest("GET", "/test", 1, 1, headers, new BodyReader {
      override def discard(): Unit = ???

      override def apply(): ByteBuffer = {
        BufferUtils.emptyBuffer
      }

      override def isExhausted: Boolean = ???
    })
    val request = Request(null, httpRequest)
    val cookies = Cookies(request)
    cookies.get("SessionId") shouldEqual Some(Cookie("SessionId", "1527771970"))
    cookies.getAll("abcdefg").size shouldEqual 2

    cookies -= "abcdefg"
    cookies.get("abcdefg") shouldEqual None

    cookies += "name" -> new Cookie("name", "test")
    cookies.get("name") shouldEqual Some(Cookie("name", "test"))

    cookies.iterator.toSeq shouldEqual Seq("name" -> Cookie("name", "test"), "SessionId" -> Cookie("SessionId", "1527771970"))
  }
}
