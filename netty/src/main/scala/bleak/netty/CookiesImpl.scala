package bleak
package netty

import io.netty.handler.codec.http.cookie.{ClientCookieDecoder, ClientCookieEncoder, ServerCookieDecoder, ServerCookieEncoder}
import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaders}

import scala.collection.JavaConverters._

private class CookiesImpl(httpHeaders: HttpHeaders, isRequest: Boolean) extends Cookies {

  def getAll(key: String): Seq[Cookie] = {
    if (isRequest) {
      val cookieStr = httpHeaders.getAsString(HttpHeaderNames.COOKIE)
      val cookies = ServerCookieDecoder.STRICT.decode(cookieStr).asScala.filter { c =>
        c.name() == key
      }.map(NettyUtils.nettyCookieToCookie)
      cookies.toSeq
    } else {
      val cookieStr = httpHeaders.getAllAsString(HttpHeaderNames.SET_COOKIE).asScala
      for (str <- cookieStr; cookie = ClientCookieDecoder.STRICT.decode(str) if cookie != null && cookie.name() == key) yield {
        NettyUtils.nettyCookieToCookie(cookie)
      }
    }
  }

  def add(cookie: Cookie): Cookies = {
    if (isRequest) {
      val cookieStr = httpHeaders.getAsString(HttpHeaderNames.COOKIE)
      val cookies = ServerCookieDecoder.STRICT.decode(cookieStr).asScala.filter { c =>
        c.name() != cookie.name
      }
      cookies.add(NettyUtils.cookieToNettyCookie(cookie))
      httpHeaders.set(HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(cookies.asJava))
    } else {
      val setCookieValue = ServerCookieEncoder.STRICT.encode(NettyUtils.cookieToNettyCookie(cookie))
      val cookies = httpHeaders.getAllAsString(HttpHeaderNames.SET_COOKIE).asScala
      val newCookies = for (c <- cookies;
                            decoded = ClientCookieDecoder.STRICT.decode(c)
                            if c != null && decoded.name() == cookie.name) yield {
        decoded
      }
      httpHeaders.remove(HttpHeaderNames.SET_COOKIE)
      httpHeaders.add(HttpHeaderNames.SET_COOKIE, setCookieValue)
      newCookies.foreach { c =>
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
      val cookies = httpHeaders.getAllAsString(HttpHeaderNames.SET_COOKIE).stream().filter { str =>
        ClientCookieDecoder.STRICT.decode(str).name() != key
      }
      httpHeaders.remove(HttpHeaderNames.SET_COOKIE)
      cookies.forEach { c =>
        httpHeaders.add(HttpHeaderNames.SET_COOKIE, c)
      }
    }
    this
  }

  def get(key: String): Option[Cookie] = {
    getAll(key).headOption
  }

  def iterator: Iterator[(String, Cookie)] = {
    if (isRequest) {
      ServerCookieDecoder.STRICT
        .decode(httpHeaders.getAsString(HttpHeaderNames.COOKIE))
        .asScala
        .map(c => c.name() -> NettyUtils.nettyCookieToCookie(c))
        .iterator
    } else {
      httpHeaders.getAllAsString(HttpHeaderNames.SET_COOKIE).asScala.map(ClientCookieDecoder.STRICT.decode)
        .map(c => c.name() -> NettyUtils.nettyCookieToCookie(c))
        .iterator
    }
  }

}
