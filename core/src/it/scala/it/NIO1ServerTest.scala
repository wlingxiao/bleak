package it

import java.net.{InetSocketAddress, Socket}
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

import goa.channel.{Handler, HandlerContext, Server}
import goa.channel.nio1.NIO1Server

class NIO1ServerTest extends IntegrationTest {

  var server: Server = _

  test("服务器启动，关闭和连接") {
    val ret = new AtomicInteger(0)
    server = NIO1Server { ch =>
      ret.getAndIncrement()
      ch.pipeline.addLast(new Handler {
        override def received(ctx: HandlerContext, msg: Object): Unit = {
          ctx.write(ByteBuffer.wrap("one".getBytes()))
        }
      })
    }
    server.start("localhost", 8085)
    val socket = new Socket()
    socket.connect(new InetSocketAddress("localhost", 8085))
    socket.getOutputStream.write("tow".getBytes())
    val one = new Array[Byte](3)
    socket.getInputStream.read(one)
    ret.get() shouldEqual 1
    one shouldEqual Array('o', 'n', 'e')
  }

  test("读取客户端输入后写入客户端") {
    server = NIO1Server { ch =>
      ch.pipeline.addLast(new Handler {
        override def received(ctx: HandlerContext, msg: Object): Unit = {
          ctx.write(msg)
        }
      })
    }
    server.start("localhost", 8085)
    val socket = new Socket()
    socket.connect(new InetSocketAddress("localhost", 8085))
    socket.getOutputStream.write("one".getBytes())
    val one = new Array[Byte](3)
    socket.getInputStream.read(one)
    one shouldEqual Array('o', 'n', 'e')
  }

  after {
    server.stop()
  }

}
