package goa

import java.nio.ByteBuffer

import goa.http1.{BodyReader, HttpRequest}
import goa.util.BufferUtils

class RequestProxyTests extends BaseTests {

  test("proxy headers") {
    val headers = Seq("User-Agent" -> "Firefox")
    val httpRequest = HttpRequest("GET", "/test", 1, 1, headers, new BodyReader {
      override def discard(): Unit = ???

      override def apply(): ByteBuffer = {
        BufferUtils.emptyBuffer
      }

      override def isExhausted: Boolean = ???
    })
    val req = Request(httpRequest)
    val proxy = new RequestProxy {
      override def request: Request = req
    }

    proxy.headers.get("User-Agent") shouldEqual Some("Firefox")
  }

}
