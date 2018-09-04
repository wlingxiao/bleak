package goa

import scala.concurrent.Future

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
    head.request(request)
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
