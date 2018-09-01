package goa

import goa.channel.{Handler, HandlerContext}
import goa.http1.{HttpRequest, HttpResponsePrelude}
import goa.logging.Logging
import goa.util.BufferUtils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

private class Dispatcher(app: Goa) extends Handler with Logging {
  override def received(ctx: HandlerContext, msg: Object): Unit = {
    val httpRequest = msg.asInstanceOf[HttpRequest]
    val request = Request(app.bodyReader, httpRequest)
    app.middlewareChain.messageReceived(request, ctx).onComplete {
      case Success(response) =>
        val prelude = HttpResponsePrelude(response.status.code, response.status.reason, response.headers.toSeq)
        val body = if (response.body != null) {
          response.body
        } else BufferUtils.emptyBuffer
        ctx.write(prelude -> body)
      case Failure(exception) =>
        exception.printStackTrace()

    }
  }
}
