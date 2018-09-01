package goa

import java.nio.ByteBuffer

import goa.http1.{BodyReader, HttpRequest}
import goa.util.BufferUtils

class RequestTests extends BaseTests {


  test("User-Agent and Content-Type") {
    val headers = Seq("User-Agent" -> "Firefox", "Content-Type" -> "text/html")
    val httpRequest = HttpRequest("GET", "/test", 1, 1, headers, new BodyReader {
      override def discard(): Unit = ???

      override def apply(): ByteBuffer = {
        BufferUtils.emptyBuffer
      }

      override def isExhausted: Boolean = ???
    })

    val request = Request(null, httpRequest)

    request.userAgent shouldEqual Some("Firefox")

    request.userAgent("Chrome")
    request.userAgent shouldEqual Some("Chrome")

    request.userAgent("IE")
    request.userAgent shouldEqual Some("IE")

    request.contentType shouldEqual Some("text/html")
  }
}
