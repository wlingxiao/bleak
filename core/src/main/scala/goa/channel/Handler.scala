package goa.channel

import scala.concurrent.Promise


trait Handler {

  def received(ctx: HandlerContext, msg: Object): Unit

  def write(ctx: HandlerContext, msg: Object, promise: Promise[Int]): Unit = {
    ctx.write(msg, promise)
  }

}

