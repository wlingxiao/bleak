package goa

import java.nio.ByteBuffer

import scala.concurrent.Future
import Response.Builder

class Context(private[goa] var prevCtx: Context,
              private[goa] var nextCtx: Context,
              private[goa] val handler: Middleware,
              private[goa] val chain: MiddlewareChain) {

  var request: Request = _

  def next(): Future[Response] = {
    if (nextCtx != null) {
      nextCtx.request = request
      nextCtx.handler.apply(nextCtx)
    } else null
  }

  def ok(body: ByteBuffer): Response = {
    Response(body = body)
  }

  def ok(): Builder = {
    new Builder()
  }

  def notFound(): Response = {
    Response(status = Status.NotFound)
  }

}

class MiddlewareChain {

  private val head: Context = new Context(null, null, (ctx: Context) => {
    ctx.next()
  }, this)

  private val tail: Context = new Context(head, null, new Middleware {
    override def apply(ctx: Context): Future[Response] = ctx.next()
  }, this)

  head.nextCtx = tail

  def use(handler: Middleware): MiddlewareChain = {
    addLast(handler)
  }

  def messageReceived(request: Request): Future[Response] = {
    head.request = request
    head.handler.apply(head)
  }

  def addLast(handler: Middleware): MiddlewareChain = {
    val prev = tail.prevCtx
    val newCtx = new Context(prev, tail, handler, this)
    newCtx.prevCtx = prev
    newCtx.nextCtx = tail
    prev.nextCtx = newCtx
    tail.prevCtx = newCtx
    this
  }
}

abstract class Middleware {

  def apply(ctx: Context): Future[Response]

}
