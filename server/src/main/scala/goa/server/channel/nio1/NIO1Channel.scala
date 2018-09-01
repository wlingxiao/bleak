package goa.server
package channel
package nio1

import java.net.SocketAddress
import java.nio.channels.SocketChannel

private[goa] case class NIO1Channel(pipeline: Pipeline,
                                    socket: SocketChannel,
                                    loop: SelectorLoop) extends Channel {
  override def local: SocketAddress = socket.getLocalAddress

  override def remote: SocketAddress = socket.getRemoteAddress

  override def toString: String = {
    s"[local=$local remote=$remote]"
  }
}
