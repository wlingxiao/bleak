package goa

class CookieTests extends BaseTests {

  test("cookie to String") {
    val cookie = new Cookie("test", "123456", "test.com", "/index", 100L, true, true)
    cookie.toString shouldEqual "test=123456, domain=test.com, path=/index, maxAge=100s, secure, HTTPOnly"
  }

}
