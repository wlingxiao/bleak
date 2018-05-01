package goa.pipeline

import goa.Logging

import scala.concurrent.Promise

private class HeadHandler extends Handler {

  override def received(ctx: Context, msg: Object): Unit = ctx.sendRead(msg)

  override def write(ctx: Context, msg: Object, promise: Promise[Int]): Unit = {
  }
}

private class TailHandler extends Handler with Logging {
  override def received(ctx: Context, msg: Object): Unit = {
    log.info("tail handler received message")
  }
}

class Pipeline {

  private val head: Context = new Context(null, null, new HeadHandler, this)

  private val tail: Context = new Context(head, null, new TailHandler, this)

  head.next = tail

  def addLast(handler: Handler): Pipeline = {
    val prev = tail.prev
    val newCtx = new Context(prev, tail, handler, this)
    newCtx.prev = prev
    newCtx.next = tail
    prev.next = newCtx
    tail.prev = newCtx
    this
  }

  def messageReceived(msg: Object): Unit = {
    head.handler.received(head, msg)
  }

}
