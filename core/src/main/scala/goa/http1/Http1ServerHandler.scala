package goa.http1

import java.nio.ByteBuffer

import goa.Logging
import goa.pipeline.{Context, Handler}

class Http1ServerHandler extends Handler with Logging {

  val codec = new Http1ServerCodec(Integer.MAX_VALUE)

  override def received(ctx: Context, msg: Object): Unit = {
    val req = codec.getRequest(ctx, msg.asInstanceOf[ByteBuffer])
    if (req != null) {
      ctx.send(req)
    }
  }
}
