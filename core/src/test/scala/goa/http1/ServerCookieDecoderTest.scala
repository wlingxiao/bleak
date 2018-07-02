package goa.http1

import goa.BaseTests

class ServerCookieDecoderTest extends BaseTests {

  var decoder = new ServerCookieDecoder

  test("decode single header") {
    val cookies = decoder.decode("name=test")
    cookies.size shouldEqual 1

    decoder.decode("name=;").size shouldEqual 1
    decoder.decode("=test;").size shouldEqual 0
    decoder.decode("").size shouldEqual 0
  }

  test("decode two element header") {
    val cookies = decoder.decode("name=test; ppp=11;")
    cookies.size shouldEqual 2
  }
}
