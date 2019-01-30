package bleak

import reflect.{ClassTag, classTag}
import scala.concurrent.Future

trait Route[I, O] {
  type Action = I => Future[O]
  private[this] var _action: Action = _

  def path: String

  def methods: Iterable[Method]

  def name: Symbol

  def metas: Map[Class[_ <: Meta], Meta]

  def meta[T <: Meta: ClassTag]: Option[T] = {
    val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    metas.get(clazz).asInstanceOf[Option[T]]
  }

  def apply(action: Action): this.type = {
    _action = action
    this
  }

  def apply(action: => Future[O]): this.type = {
    _action = _ => action
    this
  }

  def action(in: I): Future[O] = {
    require(_action != null, "Action must not be null")
    _action(in)
  }

}

case class HttpRoute(
    path: String,
    methods: Iterable[Method],
    name: Symbol,
    metas: Map[Class[_ <: Meta], Meta])
    extends Route[HttpContext, Response]

case class WebsocketRoute(path: String, name: Symbol, metas: Map[Class[_ <: Meta], Meta])
    extends Route[WebsocketContext, Response] {

  override val methods: Iterable[Method] = Seq(Method.Get)
}
