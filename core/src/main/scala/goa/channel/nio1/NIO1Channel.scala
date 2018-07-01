package goa.channel.nio1

import java.net.SocketAddress
import java.nio.channels.SocketChannel

import goa.channel.{Channel, Pipeline}

private[goa] case class NIO1Channel(pipeline: Pipeline, socket: SocketChannel) extends Channel {
  override def local: SocketAddress = socket.getLocalAddress

  override def remote: SocketAddress = socket.getRemoteAddress

  override def toString: String = {
    s"[local=$local remote=$remote]"
  }
}
