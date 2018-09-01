package goa

//import goa.http1.{ClientCookieDecoder, ClientCookieEncoder, ServerCookieDecoder, ServerCookieEncoder}

import scala.collection.mutable

abstract class Cookies extends mutable.Map[String, Cookie] with mutable.MapLike[String, Cookie, Cookies] {

  def getAll(key: String): Seq[Cookie]

  def getOrNull(key: String): Cookie = get(key).orNull

  def add(cookie: Cookie): Cookies

  override def empty: Cookies = null

  protected def message: Message
}

object Cookies {

  def apply(message: Message): Cookies = {
    null
  }

}
