package goa.server
package http1

import channel.nio1.NIO1Server
import goa.Server
import goa.server.channel.LoggingHandler


trait GoaHttpServer extends Server {
  self: goa.App =>

  private[this] var server: NIO1Server = _

  override def start(): Unit = {
    server = NIO1Server { ch =>
      ch.pipeline
        .addLast(new LoggingHandler())
        .addLast(new Http1ServerHandler)
        .addLast(new Dispatcher(this))
    }
    server.start(Host, Port)
  }

  override def close(): Unit = {
    server.stop()
  }
}
