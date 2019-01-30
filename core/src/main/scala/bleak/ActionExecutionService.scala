package bleak

import scala.concurrent.Future

private class ActionExecutionService(status: Status) extends Service[Context, Context] {
  override def apply(ctx: HttpContext): Future[HttpContext] =
    ctx.request.route match {
      case Some(route) if route.isInstanceOf[HttpRoute] =>
        ???
      case _ =>
        ???
    }
}
