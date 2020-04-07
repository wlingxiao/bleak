package bleak

import scala.collection.mutable

trait Cookies {

  def get(name: String): Option[Cookie]

  def getAll(name: String): Iterable[Cookie]

  def add(cookie: Cookie): Cookies

  def toList: List[Cookie]
}

object Cookies {

  class Impl(cookies: mutable.Map[String, Set[Cookie]]) extends Cookies {

    cookies.withDefaultValue(Set.empty)

    override def get(name: String): Option[Cookie] = getAll(name).headOption

    override def getAll(name: String): Iterable[Cookie] =
      cookies(name)

    override def add(cookie: Cookie): Cookies = {
      cookies(cookie.name) = (cookies(cookie.name) - cookie) + cookie
      this
    }

    override def toList: List[Cookie] = cookies.values.flatten.toList
  }

  def apply(cookies: Iterable[Cookie]): Cookies = {
    val map = cookies.groupBy(_.name).map { case (str, value) => str -> value.toSet }
    new Impl(mutable.Map.from(map))
  }

  def apply(cookies: Cookie*): Cookies = apply(cookies)

  def empty: Cookies = new Impl(mutable.Map.empty)

}
