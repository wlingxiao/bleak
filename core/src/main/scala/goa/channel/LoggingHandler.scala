package goa.channel

import goa.logging.Logging

class LoggingHandler extends Handler with Logging {

  override def connected(ctx: HandlerContext): Unit = {
    log.info(s"Connected: ${ctx.channel.socket.toString}")
    ctx.sendConnected()
  }

  override def received(ctx: HandlerContext, msg: Object): Unit = {
    log.info(s"Received: ${ctx.channel.socket.toString}")
    ctx.sendReceived(msg)
  }
}
