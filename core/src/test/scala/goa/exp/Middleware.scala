package goa
package exp

import java.nio.ByteBuffer

import goa.channel.HandlerContext
import goa.http1.HttpResponsePrelude

import scala.concurrent.Future

class Context(private[goa] var prevCtx: Context,
              private[goa] var nextCtx: Context,
              private[goa] val handler: Middleware,
              private[goa] val chain: MiddlewareChain) {

  var handlerContext: HandlerContext = _

  var request: Request = _

  var response: Response = _

  def next(): Future[Any] = {
    if (nextCtx != null) {
      nextCtx.request = request
      nextCtx.handlerContext = handlerContext
      nextCtx.handler.apply(nextCtx)
    } else Future.failed(new IllegalArgumentException)
  }

  def send(any: Any): Unit = {
    val prelude = HttpResponsePrelude(200, "OK", Nil)
    handlerContext.write(prelude -> ByteBuffer.wrap(any.toString.getBytes()))
  }

}

class MiddlewareChain {

  private val head: Context = new Context(null, null, (ctx: Context) => {
    ctx.next()
  }, this)

  private val tail: Context = new Context(head, null, new Middleware {
    override def apply(ctx: Context): Future[Any] = ctx.next()
  }, this)

  head.nextCtx = tail

  def use(handler: Middleware): MiddlewareChain = {
    addLast(handler)
  }

  def messageReceived(request: Request, handlerContext: HandlerContext): Future[Any] = {
    head.request = request
    head.handlerContext = handlerContext
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

  def apply(ctx: Context): Future[Any]

}
