package goa.server
package http1

import goa.logging.Logging
import channel.{Handler, HandlerContext}
import goa.util.BufferUtils
import goa.{App, Request}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

private class Dispatcher(app: App) extends Handler with Logging {
  override def received(ctx: HandlerContext, msg: Object): Unit = {
    val httpRequest = msg.asInstanceOf[HttpRequest]
    val request = Request(null, httpRequest)
    app.middlewareChain.messageReceived(request).onComplete {
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
