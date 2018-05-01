package goa.pipeline

import scala.concurrent.Promise


trait Handler {

  def received(ctx: Context, msg: Object): Unit

  def write(ctx: Context, msg: Object, promise: Promise[Int]): Unit = {
    ctx.write(msg, promise)
  }

}

