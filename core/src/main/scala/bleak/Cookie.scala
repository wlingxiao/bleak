package bleak
import io.netty.handler.codec.http.cookie
import io.netty.handler.codec.http.cookie.DefaultCookie

trait Cookie {
  def name: String

  def value: String

  def value(v: String): Cookie

  def domain: String

  def domain(d: String): Cookie

  def path: String

  def path(p: String): Cookie

  def maxAge: Long

  def maxAge(m: Long): Cookie

  def secure: Boolean

  def secure(s: Boolean): Cookie

  def httpOnly: Boolean

  def httpOnly(h: Boolean): Cookie

}

object Cookie {

  class Impl(val underlying: cookie.Cookie) extends Cookie {

    override def name: String = underlying.name()

    override def value: String = underlying.value()

    override def value(v: String): Cookie = {
      underlying.setValue(v)
      this
    }

    override def domain: String = underlying.domain()

    override def domain(d: String): Cookie = {
      underlying.setDomain(d)
      this
    }

    override def path: String = underlying.path()

    override def path(p: String): Cookie = {
      underlying.setPath(p)
      this
    }

    override def maxAge: Long =
      underlying.maxAge()

    override def maxAge(m: Long): Cookie = {
      underlying.setMaxAge(m)
      this
    }

    override def secure: Boolean = underlying.isSecure

    override def secure(s: Boolean): Cookie = {
      underlying.setSecure(s)
      this
    }

    override def httpOnly: Boolean = underlying.isHttpOnly

    override def httpOnly(only: Boolean): Cookie = {
      underlying.setHttpOnly(only)
      this
    }

    override def hashCode(): Int = underlying.hashCode()

    override def equals(obj: Any): Boolean = obj match {
      case other: Impl => other.underlying.equals(this.underlying)
      case _ => false
    }

    override def toString: String = underlying.toString

  }

  def apply(name: String, value: String): Cookie = new Impl(new DefaultCookie(name, value))

}
