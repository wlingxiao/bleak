package bleak

import scala.concurrent.Future

class AccessLogMiddleware extends Middleware with LazyLogging {

  override def apply(ctx: Context, request: Request): Future[Response] =
    ctx
      .next(request)
      .map(formatLog(request, _))(Executions.directEc)

  private def formatLog(request: Request, response: Response): Response = {
    log.info(
      s""""${request.userAgent
        .getOrElse("")}" "${request.method} ${request.uri} ${request.version}" ${response.status}"""
    )
    response
  }

}
