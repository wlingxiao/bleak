package goa.channel

import java.nio.ByteBuffer

import goa.logging.Logging

import scala.concurrent.{Future, Promise}
import scala.util.Try


class Pipeline(var channel: Channel) {

  import Pipeline._

  private val head: AbstractHandlerContext = new HeadContext(this)

  private val tail: AbstractHandlerContext = new TailContext(this)

  head.next = tail
  tail.prev = head

  def addLast(handler: Handler): Pipeline = {
    val prev = tail.prev
    val newCtx = new AbstractHandlerContextImpl(this, handler)
    newCtx.prev = prev
    newCtx.next = tail
    prev.next = newCtx
    tail.prev = newCtx
    this
  }

  def sendConnected(): Unit = {
    head.sendConnected()
  }

  def sendReceived(msg: Object): Unit = {
    head.handler.received(head, msg)
  }
}

private object Pipeline {

  class HeadContext(pipeline: Pipeline) extends AbstractHandlerContext(pipeline, null) with Handler {

    override def handler: Handler = this

    override def received(ctx: HandlerContext, msg: Object): Unit = {
      ctx.sendReceived(msg)
    }

    override def write(ctx: HandlerContext, msg: Object, promise: Promise[Int]): Unit = {
      msg match {
        case buf: ByteBuffer =>
          promise.tryComplete(Try(ctx.pipeline.channel.socket.write(buf)))
        case _ => throw new UnsupportedOperationException
      }
    }
  }

  class TailContext(pipeline: Pipeline) extends AbstractHandlerContext(pipeline, null) with Handler {
    override def received(ctx: HandlerContext, msg: Object): Unit = {

    }

    override def handler: Handler = this
  }

}
