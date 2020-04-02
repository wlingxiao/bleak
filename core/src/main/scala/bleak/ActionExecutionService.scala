package bleak

import bleak.util.Executions

import scala.concurrent.Future

class ActionExecutionService(status: Int, route: Option[Route]) extends Middleware {
  override def apply(ctx: Context, request: Request): Future[Response] =
    route match {
      case Some(value) => value.action(request)
      case _ => Future(Response(status = status))(Executions.directec)
    }
}
