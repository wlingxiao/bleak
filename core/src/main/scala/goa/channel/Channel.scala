package goa.channel

import java.nio.channels.SocketChannel

trait Channel {

  def pipeline: Pipeline

  def socket: SocketChannel

}
