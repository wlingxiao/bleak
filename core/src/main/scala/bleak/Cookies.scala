package bleak

import io.netty.handler.codec.http.cookie.{
  ClientCookieDecoder,
  ClientCookieEncoder,
  ServerCookieDecoder,
  ServerCookieEncoder
}
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaders, cookie}

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class Cookies extends mutable.Map[String, Cookie] {

  def getAll(key: String): Seq[Cookie]

  def getOrNull(key: String): Cookie = get(key).orNull

  def add(cookie: Cookie): Cookies

}

object Cookies {

  def apply(httpHeaders: HttpHeaders, isRequest: Boolean): Cookies =
    new Impl(httpHeaders, isRequest)

  final class Impl(httpHeaders: HttpHeaders, isRequest: Boolean) extends Cookies {

    def getAll(key: String): Seq[Cookie] =
      if (isRequest) {
        Option(httpHeaders.getAsString(HttpHeaderNames.COOKIE))
          .map(ServerCookieDecoder.STRICT.decode)
          .map(_.asScala)
          .getOrElse(Nil)
          .filter(_.name() == key)
          .map(nettyCookieToCookie)
          .toSeq
      } else {
        val cookieStr = httpHeaders.getAllAsString(HttpHeaderNames.SET_COOKIE).asScala
        for (str <- cookieStr; cookie = ClientCookieDecoder.STRICT.decode(str)
          if cookie != null && cookie.name() == key) yield {
          nettyCookieToCookie(cookie)
        }
      }

    def add(cookie: Cookie): Cookies = {
      if (isRequest) {
        val cookieStr = httpHeaders.getAsString(HttpHeaderNames.COOKIE)
        val cookies = ServerCookieDecoder.STRICT.decode(cookieStr).asScala.filter { c =>
          c.name() != cookie.name
        }
        cookies.add(cookieToNettyCookie(cookie))
        httpHeaders.set(HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(cookies.asJava))
      } else {
        val setCookieValue = ServerCookieEncoder.STRICT.encode(cookieToNettyCookie(cookie))
        val cookies = httpHeaders.getAllAsString(HttpHeaderNames.SET_COOKIE).asScala
        val oldCookies = for (c <- cookies;
          decoded = ClientCookieDecoder.STRICT.decode(c)
          if c != null && decoded.name() != cookie.name) yield {
          decoded
        }
        httpHeaders.remove(HttpHeaderNames.SET_COOKIE)
        httpHeaders.add(HttpHeaderNames.SET_COOKIE, setCookieValue)
        oldCookies.foreach { c =>
          httpHeaders.add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(c))
        }
      }
      this
    }

    def +=(kv: (String, Cookie)): this.type = {
      require(kv._1 == kv._2.name)
      add(kv._2)
      this
    }

    def -=(key: String): this.type = {
      if (isRequest) {
        val cookieStr = httpHeaders.getAsString(HttpHeaderNames.COOKIE)
        val cookies = ServerCookieDecoder.STRICT.decode(cookieStr).asScala.filter { c =>
          c.name() != key
        }
        httpHeaders.set(HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(cookies.asJava))
      } else {
        val cookies = httpHeaders.getAllAsString(HttpHeaderNames.SET_COOKIE).stream().filter {
          str =>
            ClientCookieDecoder.STRICT.decode(str).name() != key
        }
        httpHeaders.remove(HttpHeaderNames.SET_COOKIE)
        cookies.forEach { c =>
          httpHeaders.add(HttpHeaderNames.SET_COOKIE, c)
        }
      }
      this
    }

    def get(key: String): Option[Cookie] =
      getAll(key).headOption

    def iterator: Iterator[(String, Cookie)] =
      if (isRequest) {
        Option(httpHeaders.getAsString(HttpHeaderNames.COOKIE))
          .map(ServerCookieDecoder.STRICT.decode)
          .map(_.asScala)
          .getOrElse(Nil)
          .map(c => c.name() -> nettyCookieToCookie(c))
          .iterator
      } else {
        httpHeaders
          .getAllAsString(HttpHeaderNames.SET_COOKIE)
          .asScala
          .map(ClientCookieDecoder.STRICT.decode)
          .map(c => c.name() -> nettyCookieToCookie(c))
          .iterator
      }
  }

  def nettyCookieToCookie(nettyCookie: cookie.Cookie): bleak.Cookie =
    bleak.Cookie(
      nettyCookie.name(),
      nettyCookie.value(),
      nettyCookie.domain(),
      nettyCookie.path(),
      nettyCookie.maxAge(),
      nettyCookie.isSecure,
      nettyCookie.isHttpOnly)

  def cookieToNettyCookie(c: bleak.Cookie): cookie.Cookie = {
    val nettyCookie = new cookie.DefaultCookie(c.name, c.value)
    nettyCookie.setDomain(c.domain)
    nettyCookie.setPath(c.path)
    nettyCookie.setMaxAge(c.maxAge)
    nettyCookie.setSecure(c.secure)
    nettyCookie.setHttpOnly(c.httpOnly)
    nettyCookie
  }

}
