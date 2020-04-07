package bleak

import io.netty.handler.codec.http.{HttpHeaderNames, cookie}
import scala.jdk.CollectionConverters._
import io.netty.handler.codec.http.cookie.{
  ClientCookieDecoder,
  ClientCookieEncoder,
  ServerCookieDecoder,
  ServerCookieEncoder
}

object CookieCodec {

  def decodeRequestCookie(headers: Headers): Cookies =
    Cookies(
      headers
        .get(HttpHeaderNames.COOKIE)
        .map(ServerCookieDecoder.STRICT.decode)
        .map(_.asScala)
        .getOrElse(Nil)
        .map(nettyCookieToCookie))

  def encodeRequestCookie(headers: Headers, cookies: Cookies): Headers = {
    val encoded = ClientCookieEncoder.STRICT
      .encode(
        cookies.toList
          .map(cookieToNettyCookie)
          .asJava)
    headers.set(HttpHeaderNames.COOKIE, encoded)
  }

  def decodeResponseCookie(headers: Headers): Cookies =
    Cookies(
      headers
        .getAll(HttpHeaderNames.SET_COOKIE.toString)
        .map(ClientCookieDecoder.STRICT.decode)
        .map(nettyCookieToCookie))

  def encodeResponseCookie(headers: Headers, cookies: Cookies): Headers = {
    val encoded = cookies.toList
      .map(cookieToNettyCookie)
      .map(ServerCookieEncoder.STRICT.encode)
    headers
      .remove(HttpHeaderNames.SET_COOKIE)
      .add(HttpHeaderNames.SET_COOKIE, encoded)
  }

  def nettyCookieToCookie(nettyCookie: cookie.Cookie): Cookie = new Cookie.Impl(nettyCookie)

  def cookieToNettyCookie(c: Cookie): cookie.Cookie = c match {
    case impl: Cookie.Impl => impl.underlying
    case _ => throw new IllegalStateException()
  }

}
