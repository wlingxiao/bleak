package goa

class CookieTests extends BaseTests {

  test("cookie to String") {
    val cookie = new Cookie("test", "123456", "test.com", "/index", 100L, true, true)
    cookie.toString shouldEqual "test=123456, domain=test.com, path=/index, maxAge=100s, secure, HTTPOnly"
  }

  test("cookie comparison") {
    val a = new Cookie("test", "123456", domain = "test.com", path = "/a")
    val b = new Cookie("test", "123456", domain = "test.com", path = "/b")

    a < b should be(true)

    val c = new Cookie("test", "123456", domain = "c", path = "/")
    val d = new Cookie("test", "123456", domain = "d", path = "/")

    c < d should be(true)
  }

}
