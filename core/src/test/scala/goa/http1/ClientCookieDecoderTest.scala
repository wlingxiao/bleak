package goa.http1

import goa.BaseTests

class ClientCookieDecoderTest extends BaseTests {

  test("decode client cookie") {
    val decoder = new ClientCookieDecoder
    val cookie = decoder.decode("H_PS_PSSID=1447_21112_22072; path=/; domain=.baidu.com")
    cookie.name shouldEqual "H_PS_PSSID"
    cookie.value shouldEqual "1447_21112_22072"
    cookie.path shouldEqual "/"
    cookie.domain shouldEqual ".baidu.com"
  }

}
