package bleak

import bleak.util.Executions

import scala.concurrent.Future

class AccessLogMiddleware[C <: Context] extends Middleware[C, Context] with LazyLogging {

  override def apply(ctx: C, service: Service[C, Context]): Future[Context] =
    service(ctx).map { ctx =>
      log.info(
        s""""${ctx.userAgent
          .getOrElse("")}" "${ctx.method.name.toUpperCase} ${ctx.uri} ${ctx.version}" ${ctx.status}"""
      )
      ctx
    }(Executions.directec)
}
