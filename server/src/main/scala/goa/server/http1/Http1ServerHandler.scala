package goa.server
package http1

import java.nio.ByteBuffer

import goa.logging.Logging
import channel.{Handler, HandlerContext}

import scala.concurrent.Promise

class Http1ServerHandler extends Handler with Logging {

  var codec: Http1ServerCodec = _

  override def received(ctx: HandlerContext, msg: Object): Unit = {
    if (codec == null) {
      codec = new Http1ServerCodec(Integer.MAX_VALUE, ctx)
    }
    val req = codec.getRequest(ctx, msg.asInstanceOf[ByteBuffer])
    if (req != null) {
      ctx.sendReceived(req)
    }
  }

  override def write(ctx: HandlerContext, msg: Object, promise: Promise[Int]): Unit = {
    val (prelude, body) = msg.asInstanceOf[(HttpResponsePrelude, ByteBuffer)]
    val writer = codec.getEncoder(false, prelude)
    writer.write(body)
    writer.flush()
    writer.close()
    codec.reset()
  }
}
