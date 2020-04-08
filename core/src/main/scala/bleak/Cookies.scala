package bleak

trait Cookies {

  def get(name: String): Option[Cookie]

  def add(cookie: Cookie): Cookies

  def toSet: Set[Cookie]
}

object Cookies {

  class Impl(cookies: Map[String, Cookie]) extends Cookies {

    override def get(name: String): Option[Cookie] = cookies.get(name)

    override def add(cookie: Cookie): Cookies = new Impl(cookies + (cookie.name -> cookie))

    override def toSet: Set[Cookie] = cookies.values.toSet
  }

  def apply(cookies: Iterable[Cookie]): Cookies = new Impl(cookies.map(c => c.name -> c).toMap)

  def apply(cookies: Cookie*): Cookies = apply(cookies)

  def empty: Cookies = apply()

}
