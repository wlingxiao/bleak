package goa

import goa.logging.Logging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AccessLogMiddleware extends Middleware with Logging {
  override def apply(ctx: Context): Future[Response] = {
    ctx.next().map { response =>
      val request = ctx.request
      log.info(s""""${request.userAgent.getOrElse("")}" "${request.method.name.toUpperCase} ${request.uri} ${request.version}" ${response.status}""")
      response
    }
  }
}
