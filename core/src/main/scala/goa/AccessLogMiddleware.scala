package goa

import goa.logging.Logging
import goa.util.Executions

import scala.concurrent.{ExecutionContext, Future}

class AccessLogMiddleware extends Middleware with Logging {

  protected implicit val ec: ExecutionContext = Executions.directec

  override def apply(ctx: Context): Future[Response] = {
    ctx.next().map { response =>
      val request = ctx.request
      log.info(s""""${request.userAgent.getOrElse("")}" "${request.method.name.toUpperCase} ${request.uri} ${request.version}" ${response.status}""")
      response
    }
  }
}
