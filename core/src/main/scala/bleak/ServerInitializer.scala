package bleak

import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}

class ServerInitializer(app: Application, maxContentLength: Int)
    extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit =
    ch.pipeline()
      .addLast(new HttpServerCodec())
      .addLast(new HttpObjectAggregator(maxContentLength))
      .addLast(new RoutingHandler(app))
}
