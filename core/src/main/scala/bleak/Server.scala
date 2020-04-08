package bleak

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, ChannelHandler, ChannelOption}
import io.netty.handler.logging.LoggingHandler

trait Server { self: Application =>
  val bossGroup = new NioEventLoopGroup(1)

  val workerGroup = new NioEventLoopGroup

  private var channel: Channel = _

  val Backlog: Int = 1024

  val MaxContentLength: Int = Short.MaxValue

  val host: String = "127.0.0.1"

  val port: Int = 5000

  def start(): Unit = start(host, port)

  def start(host: String, port: Int): Unit =
    synchronized {
      if (channel == null) {
        val bootstrap = new ServerBootstrap()
        bootstrap.option[Integer](ChannelOption.SO_BACKLOG, Backlog)
        bootstrap
          .group(bossGroup, workerGroup)
          .channel(classOf[NioServerSocketChannel])
          .handler(new LoggingHandler())
          .childHandler(createInitializer())
        channel = bootstrap.bind(host, port).sync().channel()
        log.info(s"Server started at http://$host:$port")
      }
    }

  def stop(): Unit = {
    if (channel == null) {
      throw new IllegalStateException("Channel has't been initialized!")
    }
    channel.close().sync()
  }

  def createInitializer(): ChannelHandler = new ServerInitializer(this, MaxContentLength)
}
