package goa
package netty

import io.netty.handler.codec.http.cookie

private object NettyUtils {

  def nettyCookieToCookie(nettyCookie: cookie.Cookie): goa.Cookie = {
    goa.Cookie(nettyCookie.name(),
      nettyCookie.value(),
      nettyCookie.domain(),
      nettyCookie.path(),
      nettyCookie.maxAge(),
      nettyCookie.isSecure,
      nettyCookie.isHttpOnly)
  }

  def cookieToNettyCookie(goaCookie: goa.Cookie): cookie.Cookie = {
    val nettyCookie = new cookie.DefaultCookie(goaCookie.name, goaCookie.value.orNull)
    nettyCookie.setDomain(goaCookie.domain.orNull)
    nettyCookie.setPath(goaCookie.path.orNull)
    nettyCookie.setMaxAge(goaCookie.maxAge)
    nettyCookie.setSecure(goaCookie.secure)
    nettyCookie.setHttpOnly(goaCookie.httpOnly)
    nettyCookie
  }

}
