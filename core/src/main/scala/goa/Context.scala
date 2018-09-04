package goa

import java.util.concurrent.atomic.AtomicReference

import goa.Response.Builder

import scala.concurrent.Future

class Context(private[goa] var prevCtx: Context,
              private[goa] var nextCtx: Context,
              private[goa] val handler: Middleware,
              private[goa] val chain: MiddlewareChain) {

  private val requestRef = new AtomicReference[Request]()

  def request: Request = requestRef.get()

  def request(value: Request): Context = {
    requestRef.set(value)
    this
  }

  def next(): Future[Response] = {
    if (nextCtx != null) {
      nextCtx.request(request)
      nextCtx.handler.apply(nextCtx)
    } else null
  }

  def ok(body: Buf): Response = {
    Response(body = body)
  }

  def ok(): Builder = {
    request.router.attr(Symbol("MediaType"))
        .map(x => new Builder().contentType(x.asInstanceOf[String]))
        .getOrElse(new Builder())
  }

  def notFound(): Response = {
    Response(status = Status.NotFound)
  }

}
