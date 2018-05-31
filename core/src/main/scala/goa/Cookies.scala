package goa

import goa.http1.cookie.{StrictClientCookieDecoder, StrictClientCookieEncoder, StrictServerCookieDecoder, StrictServerCookieEncoder}

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class Cookies extends mutable.Map[String, Cookie] with mutable.MapLike[String, Cookie, Cookies] {

  def getAll(key: String): Seq[Cookie]

  def getOrNull(key: String): Cookie = get(key).orNull

  def add(cookie: Cookie): Cookies

  override def empty: Cookies = new CookiesImpl(Request(null), CookieCodecImpl)
}

object Cookies {

  def apply(message: Message): Cookies = {
    new CookiesImpl(message, CookieCodecImpl)
  }

}

private abstract class CookieCodec {
  def encodeClient(cookies: Iterable[Cookie]): String

  def encodeServer(cookie: Cookie): String

  def decodeClient(header: String): Option[Iterable[Cookie]]

  def decodeServer(header: String): Option[Iterable[Cookie]]
}

private object CookieCodecImpl extends CookieCodec {

  private[this] val clientEncoder = StrictClientCookieEncoder
  private[this] val serverEncoder = StrictServerCookieEncoder
  private[this] val clientDecoder = StrictClientCookieDecoder
  private[this] val serverDecoder = StrictServerCookieDecoder

  override def encodeClient(cookies: Iterable[Cookie]): String = {
    if (cookies.isEmpty) ""
    else clientEncoder.encode(cookies.asJava)
  }

  override def encodeServer(cookie: Cookie): String = serverEncoder.encode(cookie)

  override def decodeClient(header: String): Option[Iterable[Cookie]] = {
    val cookie = clientDecoder.decode(header)
    if (cookie != null) Some(Seq(cookie))
    else None
  }

  override def decodeServer(header: String): Option[Iterable[Cookie]] = {
    val cookies = serverDecoder.decode(header).asScala
    if (cookies.nonEmpty) Some(cookies)
    else None
  }
}

private final class CookiesImpl(message: Message, cookieCodec: CookieCodec) extends Cookies {

  private[this] val underlying =
    mutable.Map[String, Set[Cookie]]().withDefaultValue(Set.empty)

  private def cookieHeaderName = if (isRequest) {
    Fields.Cookie
  } else {
    Fields.SetCookie
  }

  for {
    cookieHeader <- message.headers.getAll(cookieHeaderName)
    cookie <- decodeCookies(cookieHeader)
  } {
    add(cookie)
  }

  private[this] def decodeCookies(header: String): Iterable[Cookie] = {
    val decoding = if (isRequest) {
      cookieCodec.decodeServer(header)
    } else {
      cookieCodec.decodeClient(header)
    }
    decoding.getOrElse(Nil)
  }

  private[this] def isRequest: Boolean = message.isInstanceOf[Request]

  override def getAll(key: String): Seq[Cookie] = underlying(key).toSeq

  def add(name: String, cookie: Cookie) {
    underlying(name) = (underlying(name) - cookie) + cookie
    rewriteCookieHeaders()
  }

  override def add(cookie: Cookie): Cookies = {
    add(cookie.name, cookie)
    this
  }

  override def +=(kv: (String, Cookie)): this.type = {
    val (n, c) = kv
    underlying(n) = Set(c)
    rewriteCookieHeaders()
    this
  }

  override def -=(key: String): this.type = {
    underlying -= key
    rewriteCookieHeaders()
    this
  }

  override def get(key: String): Option[Cookie] = getAll(key).headOption

  override def iterator: Iterator[(String, Cookie)] =
    for {
      (name, cookies) <- underlying.iterator
      cookie <- cookies
    } yield (name, cookie)

  protected def rewriteCookieHeaders() {
    message.headers.remove(cookieHeaderName)
    if (isRequest) {
      message.headers.set(cookieHeaderName, cookieCodec.encodeClient(values))
    } else {
      foreach {
        case (_, cookie) =>
          message.headers.add(cookieHeaderName, cookieCodec.encodeServer(cookie))
      }
    }
  }

}