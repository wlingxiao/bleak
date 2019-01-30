package bleak
package netty

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.{HttpMessage, HttpObjectAggregator, HttpRequest}

import scala.concurrent.Future

private class OversizedMessageHandler(
    maxContentLength: Int,
    app: Application,
    route: Option[Route[_, _]])
    extends HttpObjectAggregator(maxContentLength) {
  import OversizedMessageHandler._

  private val responseWriter = new ResponseWriter

  override def handleOversizedMessage(ctx: ChannelHandlerContext, oversized: HttpMessage): Unit =
    oversized match {
      case req: HttpRequest =>
        val routeRequest = new RoutingHandler.RouteRequest(ctx, Request(req), route, app)
        val routeService = new OversizedMessageActionExecutionService
        val future = app.received(routeRequest, routeService)
        responseWriter.write(ctx, routeRequest, future)
      case _ => super.handleOversizedMessage(ctx, oversized)
    }

}

private object OversizedMessageHandler {
  private class OversizedMessageActionExecutionService extends Service[Context, Context] {
    override def apply(ctx: Context): Future[Context] = {
      val res = ctx.response
      res.status = Status.RequestEntityTooLarge
      Future.successful(ctx.response(res))
    }
  }
}
