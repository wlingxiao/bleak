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
                   var httpOnly: Boolean = false)

object Cookie {

  def newCookie(name: String, value: String): Cookie = {
    new Cookie(name, value)
  }
}
