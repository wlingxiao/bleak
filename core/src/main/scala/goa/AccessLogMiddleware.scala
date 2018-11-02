package goa

import goa.logging.Logging
import goa.util.Executions

import scala.concurrent.{ExecutionContext, Future}

class AccessLogMiddleware extends Middleware with Logging {

  protected implicit val ec: ExecutionContext = Executions.directec

  override def apply(ctx: Context): Future[Context] = {
    ctx.next() map { ctx =>
      log.info(s""""${ctx.userAgent.getOrElse("")}" "${ctx.method.name.toUpperCase} ${ctx.uri} ${ctx.version}" ${ctx.status}""")
      ctx
    }
  }
}
