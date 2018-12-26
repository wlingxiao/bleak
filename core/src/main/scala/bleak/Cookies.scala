package bleak


import scala.collection.mutable

abstract class Cookies extends mutable.Map[String, Cookie] with mutable.MapLike[String, Cookie, Cookies] {

  def getAll(key: String): Seq[Cookie]

  def getOrNull(key: String): Cookie = get(key).orNull

  def add(cookie: Cookie): Cookies

  override def empty: Cookies = Cookies(Set.empty)

}

object Cookies {

  def empty: Cookies = apply(Set.empty)

  final class Impl(cookies: Set[Cookie]) extends Cookies {

    private[this] val underlying =
      mutable.Map[String, Set[Cookie]]().withDefaultValue(Set.empty)

    cookies.foreach(add)

    override def getAll(key: String): Seq[Cookie] = underlying(key).toSeq

    private def add(name: String, cookie: Cookie) {
      underlying(name) = (underlying(name) - cookie) + cookie
    }

    override def add(cookie: Cookie): Cookies = {
      add(cookie.name, cookie)
      this
    }

    override def +=(kv: (String, Cookie)): this.type = {
      val (n, c) = kv
      underlying(n) = Set(c)
      this
    }

    override def -=(key: String): this.type = {
      underlying -= key
      this
    }

    override def get(key: String): Option[Cookie] = getAll(key).headOption

    override def iterator: Iterator[(String, Cookie)] =
      for {
        (name, cookies) <- underlying.iterator
        cookie <- cookies
      } yield (name, cookie)
  }

  def apply(cookies: Set[Cookie]): Cookies = {
    new Impl(cookies)
  }

}



