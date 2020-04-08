package bleak

class CookiesSpec extends Spec {

  "Cookies.get" should {
    "return none if cookie is not present" in {
      val cookies = Cookies.empty
      cookies.get("foo") should_=== None
    }

    "get a cookie by name" in {
      val cookies = Cookies(Cookie("foo", "bar"), Cookie("hello", "world"))
      cookies.get("foo") should_=== Some(Cookie("foo", "bar"))
    }
  }

  "Cookies.add" should {
    "add cookie" in {
      val cookies = Cookies.empty.add(Cookie("foo", "bar"))
      cookies.get("foo") should_=== Some(Cookie("foo", "bar"))
    }
  }

  "Cookies.toSet" should {
    "convert cookies to set" in {
      val cookies = Cookies(Cookie("foo", "bar"), Cookie("foo", "bar1"), Cookie("hello", "world"))
      cookies.toSet should_== Set(Cookie("foo", "bar"), Cookie("hello", "world"))
    }
  }

}
