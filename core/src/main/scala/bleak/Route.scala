package bleak

import io.netty.handler.codec.http.HttpMethod

import scala.concurrent.Future

trait Route {
  type Action = Request => Future[Response]
  private[this] var _action: Action = _

  def path: String

  def method: HttpMethod

  def apply(action: Action): this.type = {
    _action = action
    this
  }

  def apply(action: => Future[Response]): this.type = {
    _action = _ => action
    this
  }

  def action(in: Request): Future[Response] = {
    require(_action != null, "Action must not be null")
    _action(in)
  }

}

case class HttpRoute(path: String, method: HttpMethod) extends Route

case class WebsocketRoute(path: String) extends Route {

  override val method: HttpMethod = HttpMethod.GET

}
