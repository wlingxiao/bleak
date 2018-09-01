package goa.channel.nio1

import java.net.InetSocketAddress
import java.nio.channels.{ServerSocketChannel, SocketChannel}
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.{ExecutorService, Executors, ThreadFactory}

import goa.channel.{Channel, Initializer, Pipeline, Server}
import goa.logging.Logging

class NIO1Server(
                  executor: ExecutorService,
                  initializer: Initializer,
                  poolSize: Int,
                  bufferSize: Int)
  extends Server with Logging {

  private val serverChannel = openServerChannel

  private val acceptor: Acceptor = new Acceptor(executor, serverChannel, initializer, poolSize, bufferSize)

  acceptor.setName("Acceptor")

  override def start(host: String, port: Int): Unit = {
    log.info(s"Http server start on $port")
    serverChannel.bind(new InetSocketAddress(host, port))
    serverChannel.configureBlocking(false)
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

  private def openServerChannel: ServerSocketChannel = {
    val s = ServerSocketChannel.open()
    s.configureBlocking(false)
    s
  }

}

object NIO1Server {

  def apply(initializer: Initializer): NIO1Server = {
    new NIO1Server(Executors.newFixedThreadPool(DefaultPoolSize, new ThreadFactory {
      private[this] val next = new AtomicInteger(0)

      override def newThread(r: Runnable): Thread = {
        val id = next.getAndIncrement()
        val thread = new Thread(r, s"Selector-$id")
        thread
      }
    }), initializer, DefaultPoolSize, DefaultBufferSize)
  }

  private val DefaultPoolSize: Int =
    math.max(4, Runtime.getRuntime.availableProcessors() + 1)

  private val DefaultBufferSize: Int = 1024

}