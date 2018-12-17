package bleak
package netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.{Channel, ChannelHandler, ChannelInitializer, ChannelOption}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpServerCodec}
import io.netty.handler.logging.LoggingHandler

import scala.collection.mutable.ArrayBuffer

trait Netty extends Goa {

  protected val bossGroup = new NioEventLoopGroup(1)

  protected val workerGroup = new NioEventLoopGroup

  private var channel: Channel = _

  val middlewares = ArrayBuffer[() => Middleware]()

  val Backlog: Int = 1024

  val MaxContentLength: Int = Short.MaxValue

  override def use(middleware: => Middleware): this.type = {
    middlewares += (() => middleware)
    this
  }

  override def sessionManager: SessionManager = new InMemorySessionManager()

  override def run(): Unit = {
    synchronized {
      initModules()
      start()
      channel.closeFuture().sync()
    }
  }

  override def stop(): Unit = {
    synchronized {
      channel.close()
    }
  }

  override def start(): Unit = {
    synchronized {
      if (channel == null) {
        val bootstrap = new ServerBootstrap()
        bootstrap.option[Integer](ChannelOption.SO_BACKLOG, Backlog)
        bootstrap.group(bossGroup, workerGroup)
          .channel(classOf[NioServerSocketChannel])
          .handler(new LoggingHandler())
          .childHandler(createInitializer())
        log.info(s"Server started on port $Port")
        channel = bootstrap.bind(Host, Port).sync().channel()
      }
    }
  }

  def createInitializer(): ChannelHandler = {
    new NettyInitializer(this, MaxContentLength)
  }

  override def close(): Unit = {
    stop()
  }
}

class NettyInitializer(app: Netty, maxContentLength: Int) extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit = {
    ch.pipeline()
      .addLast(new HttpServerCodec())
      .addLast(new RouteHandler(app))
  }
}