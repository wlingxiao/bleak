package goa.channel

import java.net.{InetSocketAddress, Socket}
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

import goa.BaseTests
import goa.channel.nio1.NIO1Server
import goa.pipeline.Context

class NIO1ServerTests extends BaseTests {

  var server: Server = _

  before {

  }

  test("服务器启动，关闭和连接") {
    val ret = new AtomicInteger(0)
    server = NIO1Server { ch =>
      ret.getAndIncrement()
      ch.pipeline.addLast((ctx: Context, msg: Object) => {
        ctx.write(ByteBuffer.wrap("one".getBytes()))
      })
    }
    server.start("localhost", 8085)
    val socket = new Socket()
    socket.connect(new InetSocketAddress("localhost", 8085))
    socket.getOutputStream.write("tow".getBytes())
    val one = new Array[Byte](3)
    socket.getInputStream.read(one)
    Thread.sleep(1000)
    ret.get() shouldEqual 1
    one shouldEqual Array('o', 'n', 'e')
  }

  test("读取客户端输入后写入客户端") {
    server = NIO1Server { ch =>
      ch.pipeline.addLast((ctx: Context, msg: Object) => {
        ctx.write(msg)
      })
    }
    server.start("localhost", 8085)
    val socket = new Socket()
    socket.connect(new InetSocketAddress("localhost", 8085))
    socket.getOutputStream.write("one".getBytes())
    val one = new Array[Byte](3)
    socket.getInputStream.read(one)
    Thread.sleep(1000)
    one shouldEqual Array('o', 'n', 'e')
  }

  after {
    server.stop()
  }

}

