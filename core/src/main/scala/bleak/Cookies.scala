package bleak

trait Cookies {

  def get(name: String): Option[Cookie]

  def getAll(name: String): Iterable[Cookie]

  def add(cookie: Cookie): Cookies

  def toList: List[Cookie]
}

object Cookies {

  class Impl(cookies: Iterable[Cookie]) extends Cookies {

    override def get(name: String): Option[Cookie] = cookies.find(_.name == name)

    override def getAll(name: String): Iterable[Cookie] = cookies.filter(_.name == name)

    override def add(cookie: Cookie): Cookies =
      new Impl(cookies ++ Iterable(cookie))

    override def toList: List[Cookie] = cookies.toList
  }

  def apply(cookies: Iterable[Cookie]): Cookies = new Impl(cookies)

  def apply(cookies: Cookie*): Cookies = apply(cookies)

  def empty: Cookies = new Impl(Nil)

}
