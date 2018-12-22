package bleak

import reflect.{ClassTag, classTag}
import scala.concurrent.Future

trait Route {

  def path: String

  def methods: Iterable[Method]

  def name: String

  def meta[T <: Meta : ClassTag]: Option[T]

  type Action

  def apply(ac: Action): Route

  def apply(ret: => Future[Result]): Route

  def maxContentLength: Int = Int.MaxValue

}

case class HttpRoute(path: String, methods: Iterable[Method], name: String, metas: Map[Class[_ <: Meta], Meta]) extends Route {

  override type Action = Context => Future[Result]

  var action: Action = _

  override def apply(ac: Action): this.type = {
    action = ac
    this
  }

  override def apply(ret: => Future[Result]): this.type = {
    action = _ => ret
    this
  }

  def meta[T <: Meta : ClassTag]: Option[T] = {
    val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    metas.get(clazz).asInstanceOf[Option[T]]
  }
}

case class WebSocketRoute(path: String, name: String, metas: Map[Class[_ <: Meta], Meta]) extends Route {

  override def methods: Iterable[Method] = Seq(Method.Get)

  def meta[T <: Meta : ClassTag]: Option[T] = {
    val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    metas.get(clazz).asInstanceOf[Option[T]]
  }

  override type Action = WebSocketContext => Future[Result]

  var action: Action = _

  def apply(ac: Action): this.type = {
    action = ac
    this
  }

  def apply(ret: => Future[Result]): this.type = {
    action = _ => ret
    this
  }
}
