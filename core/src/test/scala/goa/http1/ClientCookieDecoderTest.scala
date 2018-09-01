package goa.http1

import goa.BaseTests

class ClientCookieDecoderTest extends BaseTests {

  test("decode client cookie") {
    val decoder = new ClientCookieDecoder
    val cookie = decoder.decode("name=test; Max-Age=10086; Expires=Fri, 06 Jul 2018 13:08:58 GMT; Domain=example.com; Path=/555; Secure; HTTPOnly")
    cookie.name shouldEqual "name"
    cookie.value shouldEqual Some("test")
    cookie.domain shouldEqual Some("example.com")
    cookie.path shouldEqual Some("/555")
    cookie.secure shouldBe true
    cookie.httpOnly shouldBe true
  }

}
