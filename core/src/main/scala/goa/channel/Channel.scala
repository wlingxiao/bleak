package goa.channel

import java.nio.channels.SocketChannel

import goa.pipeline.Pipeline

trait Channel {

  def pipeline: Pipeline

  def socket: SocketChannel

}
