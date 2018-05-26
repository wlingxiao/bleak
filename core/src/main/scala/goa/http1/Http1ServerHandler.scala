package goa.http1

import java.nio.ByteBuffer

import goa.Logging
import goa.pipeline.{Context, Handler}

import scala.concurrent.Promise

class Http1ServerHandler extends Handler with Logging {

  var codec: Http1ServerCodec = _

  override def received(ctx: Context, msg: Object): Unit = {
    if (codec == null) {
      codec = new Http1ServerCodec(Integer.MAX_VALUE, ctx)
    }
    val req = codec.getRequest(ctx, msg.asInstanceOf[ByteBuffer])
    if (req != null) {
      ctx.send(req)
    }
  }

  override def write(ctx: Context, msg: Object, promise: Promise[Int]): Unit = {
    val (prelude, body) = msg.asInstanceOf[(HttpResponsePrelude, ByteBuffer)]
    val writer = codec.getEncoder(false, prelude)
    writer.write(body)
    writer.flush()
    writer.close()
    codec.reset()
  }
}
