package bleak
package netty

import bleak.matcher.{AntPathMatcher, PathMatcher}
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.{Channel, ChannelHandler, ChannelInitializer, ChannelOption}
import io.netty.handler.codec.http.HttpServerCodec
import io.netty.handler.logging.LoggingHandler

import scala.collection.mutable.ArrayBuffer

class Netty extends Application {

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

  val pathMatcher: PathMatcher = new AntPathMatcher

  @volatile private var _host: String = "127.0.0.1"

  @volatile private var _port: Int = 7865

  def host: String = _host

  def port: Int = _port

  def run(host: String = this.host, port: Int = this.port): Unit = {
    synchronized {
      _host = host
      _port = port
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

  def start(host: String = this.host, port: Int = this.port): Unit = {
    synchronized {
      if (channel == null) {
        val bootstrap = new ServerBootstrap()
        bootstrap.option[Integer](ChannelOption.SO_BACKLOG, Backlog)
        bootstrap.group(bossGroup, workerGroup)
          .channel(classOf[NioServerSocketChannel])
          .handler(new LoggingHandler())
          .childHandler(createInitializer())
        log.info(s"Server started on port $port")
        channel = bootstrap.bind(host, port).sync().channel()
      }
    }
  }

  def createInitializer(): ChannelHandler = {
    new NettyInitializer(this, MaxContentLength)
  }
}

class NettyInitializer(app: Netty, maxContentLength: Int) extends ChannelInitializer[SocketChannel] {
  override def initChannel(ch: SocketChannel): Unit = {
    ch.pipeline()
      .addLast(new HttpServerCodec())
      .addLast(new RouteHandler(app))
  }
}