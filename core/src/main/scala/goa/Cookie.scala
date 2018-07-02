package goa

import scala.beans.BeanProperty

case class Cookie(
                   @BeanProperty
                   var name: String,
                   @BeanProperty
                   var value: String,
                   @BeanProperty
                   var domain: String = null,
                   @BeanProperty
                   var path: String = "/",
                   @BeanProperty
                   var maxAge: Long = 0,
                   @BeanProperty
                   var secure: Boolean = true,
                   @BeanProperty
                   var httpOnly: Boolean = false,
                   @deprecated
                   var rawValue: String = null,
                   var wrap: Boolean = false)
  extends Ordered[Cookie] {

  override def compare(c: Cookie): Int = {
    var v = name.compareTo(c.name)
    if (v != 0) {
      return v
    }

    if (path eq null) {
      if (c.path ne null) {
        return -1
      }
    } else if (c.path eq null) {
      return 1
    } else {
      v = path.compareTo(c.path)
      if (v != 0) {
        return v
      }
    }

    if (domain eq null) {
      if (c.domain ne null) {
        return -1
      }
    } else if (c.domain eq null) {
      return 1
    } else {
      v = domain.compareToIgnoreCase(c.domain)
      return v
    }

    0
  }

  override def toString: String = {
    val buf = StringBuilder.newBuilder
      .append(name)
      .append('=')
      .append(value)
    if (domain != null) {
      buf.append(", domain=")
        .append(domain)
    }
    if (path != null) {
      buf.append(", path=")
        .append(path)
    }
    if (maxAge >= 0) {
      buf.append(", maxAge=")
        .append(maxAge)
        .append('s')
    }
    if (secure) {
      buf.append(", secure")
    }
    if (httpOnly) {
      buf.append(", HTTPOnly")
    }
    buf.toString()
  }

}

object Cookie {

  def apply(name: String, value: String): Cookie = {
    new Cookie(name, value)
  }

  /**
    * For java users
    */
  def newCookie(name: String, value: String): Cookie = {
    Cookie(name, value)
  }
}
