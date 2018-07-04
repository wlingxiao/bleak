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
    cookies("SessionId").name shouldEqual "SessionId"
    cookies("SessionId").value shouldEqual Some("1527771970")
    cookies.getAll("abcdefg").size shouldEqual 2

    cookies -= "abcdefg"
    cookies.get("abcdefg") shouldEqual None

    cookies += "name" -> Cookie("name", "test")
    cookies("name").name shouldEqual "name"
    cookies("name").value shouldEqual Some("test")
    val c = cookies.iterator.toArray
    c(0)._1 shouldEqual "name"
    c(0)._2.name shouldEqual "name"
    c(0)._2.value shouldEqual Some("test")

    c(1)._1 shouldEqual "SessionId"
    c(1)._2.name shouldEqual "SessionId"
    c(1)._2.value shouldEqual Some("1527771970")
  }
}
