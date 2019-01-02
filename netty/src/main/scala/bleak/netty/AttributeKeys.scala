package bleak
package netty

import io.netty.util.AttributeKey

import scala.concurrent.Future

private[netty] object AttributeKeys {
  val contextKey: AttributeKey[Future[Context]] = AttributeKey.valueOf[Future[Context]]("Context")

  val responseKey: AttributeKey[Response] = AttributeKey.valueOf[Response]("resposne")

  val appKey: AttributeKey[Application] = AttributeKey.valueOf[Application]("application")

  val webSocketRouteKey: AttributeKey[WebSocketRoute] = AttributeKey.valueOf[WebSocketRoute]("WebSocketRoute")

  val webSocketEventKey: AttributeKey[PartialFunction[Frame, Unit]] = AttributeKey.valueOf[PartialFunction[Frame, Unit]]("WebSocketEvent")

  val routeKey: AttributeKey[Option[Route]] = AttributeKey.valueOf[Option[Route]]("Route")

}
