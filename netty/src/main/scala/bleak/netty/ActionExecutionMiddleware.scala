package bleak
package netty

import java.nio.charset.Charset

import bleak.util.Executions

import scala.concurrent.{ExecutionContext, Future}

private class ActionExecutionMiddleware extends Middleware {

  import ActionExecutionMiddleware._

  protected implicit val ec: ExecutionContext = Executions.directec

  override def apply(ctx: Context): Future[Context] = {
    if (ctx.response.status == Status.Ok) {
      ctx.request.route match {
        case hr: HttpRoute =>
          hr.action(ctx).map { ret =>
            convertResult(ctx, ret, hr)
          }
        case _ => Future.successful(ctx)
      }
    } else {
      Future.successful(ctx)
    }
  }
}

private object ActionExecutionMiddleware {

  def convertResult(ctx: Context, ret: Result, route: Route): Context = {
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
        response.mimeType = route.meta[Meta.Produce]
          .flatMap(p => p.value.headOption)
          .orNull
        response.charset = route.meta[Meta.Charset]
          .map(cs => Charset.forName(cs.value))
          .orNull
      case _ =>
    }
    response.body = ret.body
    ctx.response = response
    ctx
  }

}