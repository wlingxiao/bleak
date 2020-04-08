package bleak

trait Cookies {

  def get(name: String): Option[Cookie]

  def add(cookie: Cookie): Cookies

  def remove(name: String): Cookies

  def contains(name: String): Boolean

  def toSet: Set[Cookie]
}

object Cookies {

  class Impl(cookies: Map[String, Cookie]) extends Cookies {

    override def get(name: String): Option[Cookie] = cookies.get(name)

    override def add(cookie: Cookie): Cookies = new Impl(cookies + (cookie.name -> cookie))

    override def remove(name: String): Cookies = new Impl(cookies - name)

    override def contains(name: String): Boolean = cookies.contains(name)

    override def toSet: Set[Cookie] = cookies.values.toSet
  }

  def apply(cookies: Iterable[Cookie]): Cookies = new Impl(cookies.map(c => c.name -> c).toMap)

  def apply(cookies: Cookie*): Cookies = apply(cookies)

  def empty: Cookies = apply()

}
