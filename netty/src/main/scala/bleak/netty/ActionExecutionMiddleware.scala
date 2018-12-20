package bleak
package netty

import java.nio.charset.Charset

import bleak.util.Executions

import scala.concurrent.{ExecutionContext, Future}

private[netty] class ActionExecutionMiddleware extends Middleware {

  protected implicit val ec: ExecutionContext = Executions.directec

  override def apply(ctx: Context): Future[Context] = {
    if (ctx.response.status == Status.Ok) {
      ctx.request.route match {
        case dr: HttpRoute =>
          Future {
            val ret = dr.action(ctx)
            val response = ctx.response
            response.status = ret.status
            for ((k, v) <- ret.headers) {
              response.headers.add(k, v)
            }
            for (c <- ret.cookies) {
              response.cookies.add(c)
            }

            response.mimeType match {
              case None =>
                response.mimeType = dr.meta[Meta.Produce]
                  .flatMap(p => p.value.headOption)
                  .orNull
                response.charset = dr.meta[Meta.Charset]
                  .map(cs => Charset.forName(cs.value))
                  .orNull
              case _ =>
            }
            response.body = ret.body
            ctx.response = response
            ctx
          }
        case _ => Future.successful(ctx)
      }
    } else {
      Future.successful(ctx)
    }
  }
}
