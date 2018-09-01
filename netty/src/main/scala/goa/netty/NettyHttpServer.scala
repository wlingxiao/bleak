package goa
package netty

import goa.logging.Logging
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{ChannelHandler, ChannelInitializer, ChannelOption}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.logging.LoggingHandler

trait NettyHttpServer extends Server with Logging {
  self: goa.App =>

  protected val bossGroup = new NioEventLoopGroup(1)

  protected val workerGroup = new NioEventLoopGroup

  protected val Backlog: Int = 1024

  override def start(): Unit = {
    val bootstrap = new ServerBootstrap()
    bootstrap.option[Integer](ChannelOption.SO_BACKLOG, Backlog)
    bootstrap.group(bossGroup, workerGroup)
      .channel(classOf[NioServerSocketChannel])
      .handler(new LoggingHandler())
      .childHandler(createInitializer())
    log.info(s"Server started on port $Port")
    val ch = bootstrap.bind(Host, Port).sync().channel()
    ch.closeFuture().sync()
  }

  def createInitializer(): ChannelHandler = {
    new NettyServerInitializer(this)
  }

  override def close(): Unit = ???
}

class NettyServerInitializer(app: goa.App) extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit = {
    ch.pipeline()
      .addLast(new HttpServerCodec())
      .addLast(new HttpObjectAggregator(Short.MaxValue))
      .addLast(new DispatchHandler(app))
  }
}
