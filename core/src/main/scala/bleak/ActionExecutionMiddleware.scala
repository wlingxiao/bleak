package bleak

import scala.concurrent.Future

class ActionExecutionMiddleware(status: Int, route: Option[Route]) extends Middleware {
  override def apply(ctx: Context, request: Request): Future[Response] =
    route match {
      case Some(value) => value.action(request)
      case _ => Future(Response(status = status))(Executions.directEc)
    }
}
