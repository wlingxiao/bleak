package bleak

import bleak.Route.Attribute
import reflect.{ClassTag, classTag}

trait Route {

  def path: String

  def method: Method

  def name: String

  def attr[T <: Attribute : ClassTag]: Option[T]

  type Action

  def apply(ac: Action): Route

  def apply(ret: => Result): Route

  def maxContentLength: Int = Int.MaxValue

}

case class HttpRoute(path: String, method: Method, name: String, attrs: Map[Class[_ <: Attribute], Attribute]) extends Route {

  override type Action = Context => Result

  var action: Action = _

  override def apply(ac: Action): this.type = {
    action = ac
    this
  }

  override def apply(ret: => Result): this.type = {
    action = _ => ret
    this
  }

  def attr[T <: Attribute : ClassTag]: Option[T] = {
    val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    attrs.get(clazz).asInstanceOf[Option[T]]
  }
}

case class WebSocketRoute(path: String, name: String, attrs: Map[Class[_ <: Attribute], Attribute]) extends Route {

  override def method: Method = Method.Get

  def attr[T <: Attribute : ClassTag]: Option[T] = {
    val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    attrs.get(clazz).asInstanceOf[Option[T]]
  }

  override type Action = WebSocketContext => Result

  var action: Action = _

  def apply(ac: Action): this.type = {
    action = ac
    this
  }

  def apply(ret: => Result): this.type = {
    action = _ => ret
    this
  }
}

object Route {

  trait Attribute

  case class Consume(value: String*) extends Attribute

  case class Produce(value: String*) extends Attribute

  case class Charset(value: String) extends Attribute

}
