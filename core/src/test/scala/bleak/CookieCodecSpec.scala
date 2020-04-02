package bleak

import io.netty.handler.codec.http.HttpHeaderNames
import org.specs2.mutable._

class CookieCodecSpec extends Specification {

  "CookieCodec.decodeRequestCookie" should {
    "decode" in {
      val decoded = CookieCodec.decodeRequestCookie(
        Headers.empty
          .add(HttpHeaderNames.COOKIE, "foo=bar"))

      decoded.get("foo").get.value should_== "bar"
    }
  }

  "CookieCodec.encodeRequestCookie" should {
    "encode" in {
      val encoded = CookieCodec.encodeRequestCookie(Headers.empty, Cookies(Cookie("foo", "bar")))
      encoded.get(HttpHeaderNames.COOKIE) should_=== Some("foo=bar")
    }
  }

  "CookieCodec.decodeResponseCookie" should {
    "decode" in {
      val decoded =
        CookieCodec.decodeResponseCookie(Headers.empty.add(HttpHeaderNames.SET_COOKIE, "foo=bar"))
      decoded.get("foo").get.value should_== "bar"
    }
  }

  "CookieCodec.encodeResponseCookie" should {
    "encode" in {
      val encoded = CookieCodec.encodeResponseCookie(Headers.empty, Cookies(Cookie("foo", "bar")))
      encoded.get(HttpHeaderNames.SET_COOKIE) should_=== Some("foo=bar")
    }
  }

}
