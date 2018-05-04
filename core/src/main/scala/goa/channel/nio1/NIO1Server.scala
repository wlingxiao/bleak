package goa.channel.nio1

import java.net.InetSocketAddress
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import java.util.concurrent.ExecutorService

import goa.Logging
import goa.channel.{Channel, Initializer, Server}
import goa.pipeline.Pipeline

private case class NIO1Channel(pipeline: Pipeline, socket: SocketChannel) extends Channel

class NIO1Server(
                  executor: ExecutorService,
                  initializer: Initializer)
  extends Server with Logging {

  private var serverChannel: ServerSocketChannel = _

  private var acceptor: Acceptor = _

  override def start(host: String, port: Int): Unit = {
    log.info(s"server start on $port")
    serverChannel = ServerSocketChannel.open().bind(new InetSocketAddress(host, port))
    serverChannel.configureBlocking(false)
    acceptor = new Acceptor(executor, serverChannel, initializer)
    acceptor.start()
  }

  override def join(): Unit = {
    acceptor.join()
  }

  override def stop(): Unit = {
    acceptor.close()
    serverChannel.close()
    executor.shutdown()
  }
}