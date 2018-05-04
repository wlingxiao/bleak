package goa.channel.nio1

import java.nio.ByteBuffer
import java.nio.channels.{ClosedChannelException, SelectionKey, Selector, SocketChannel}
import java.util.concurrent.{Executor, LinkedBlockingQueue}
import java.util.{Set => JSet}

import goa.Logging
import goa.channel.Channel

import scala.annotation.tailrec

class SelectorLoop(executor: Executor) extends Runnable with Logging {

  @volatile
  private var isClose = false

  private val registerTaskQueue = new LinkedBlockingQueue[Runnable]()

  private var selector: Selector = _

  private var thread: Thread = _

  def register(channel: Channel): Unit = {
    selector = Selector.open()
    registerTaskQueue.offer(new RegisterTask(channel))
    executor.execute(this)
    selector.wakeup()
  }

  override def run(): Unit = {
    thread = Thread.currentThread()
    while (!isClose) {
      val selected = selector.select(500)
      processRegisterTaskQueue()
      if (selected > 0) {
        processSelectedKeys(selector.selectedKeys())
      }
    }
  }

  private def processSelectedKeys(keys: JSet[SelectionKey]): Unit = {
    val it = keys.iterator()
    while (it.hasNext) {
      val k = it.next()
      it.remove()
      if (k.isReadable) {
        read(k)
      }
    }
  }

  private def read(k: SelectionKey): Unit = {
    val buf = ByteBuffer.allocate(1024)
    val channel = k.channel().asInstanceOf[SocketChannel]
    val sc = k.attachment().asInstanceOf[Channel]
    val r = try {
      val rr = channel.read(buf)
      sc.pipeline.messageReceived(buf)
      log.debug((sc.socket == channel) + "")
      rr
    } catch {
      case ignore: ClosedChannelException =>
        log.info(ignore.getMessage)
        -1
    }
    log.debug(s"read: $r")
    if (r < 0) {
      k.cancel()
      channel.close()
    }
  }

  @tailrec
  private def processRegisterTaskQueue(): Unit = {
    val task = registerTaskQueue.poll()
    if (task == null) {
      ()
    } else {
      task.run()
      processRegisterTaskQueue()
    }
  }

  private class RegisterTask(channel: Channel) extends Runnable {
    override def run(): Unit = {
      channel.socket.register(selector, SelectionKey.OP_READ, channel)
    }
  }

  def close(): Unit = {
    isClose = true
    log.info(s"shutting down SelectorLoop ${if (thread ne null) thread.getName else "unbind"}")
    if (selector ne null) {
      selector.wakeup()
    }
  }

}
