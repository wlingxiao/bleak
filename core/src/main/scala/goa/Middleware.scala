package goa

class Context(private[goa] var prevCtx: Context,
              private[goa] var nextCtx: Context,
              private[goa] val handler: Middleware,
              private[goa] val chain: MiddlewareChain) {

  def request: Request = Goa.request

  def response: Response = Goa.response

  def next(): Unit = {
    if (nextCtx != null) {
      nextCtx.handler.apply(nextCtx)
    }
  }

}

class MiddlewareChain {

  private val head: Context = new Context(null, null, (ctx: Context) => {
    ctx.next()
  }, this)

  private val tail: Context = new Context(head, null, new Middleware {
    override def apply(ctx: Context): Unit = ctx.next()
  }, this)

  head.nextCtx = tail

  def use(handler: Middleware): MiddlewareChain = {
    addLast(handler)
  }

  def messageReceived(): Unit = {
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

  def apply(ctx: Context): Unit

}
