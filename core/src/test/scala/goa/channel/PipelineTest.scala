package goa.channel

import goa.BaseTests

import scala.concurrent.Promise

class PipelineTest extends BaseTests {

  var pipeline: Pipeline = _

  before {
    pipeline = Pipeline()
  }

  test("空 Pipeline 不会抛出异常") {
    pipeline.sendReceived(null)
  }

  test("添加两个 Handler 发送入站消息") {
    var ret = 0
    val handler = new Handler {
      override def received(ctx: HandlerContext, msg: Object): Unit = {
        ret += 1
        ctx.sendReceived(msg)
      }
    }

    pipeline.addLast(handler)
    pipeline.addLast(handler)

    pipeline.sendReceived(null)
    ret shouldEqual 2
  }

  test("添加两个 Handler 发送出站消息") {
    var ret = 0
    pipeline.addLast(new Handler {
      override def received(ctx: HandlerContext, msg: Object): Unit = {
        ctx.sendReceived(msg)
      }

      override def write(ctx: HandlerContext, msg: Object, promise: Promise[Int]): Unit = {
        ret += 1
      }
    })

    pipeline.addLast(new Handler {
      override def received(ctx: HandlerContext, msg: Object): Unit = {
        ctx.write(msg)
      }
    })

    pipeline.sendReceived(null)

    ret shouldEqual 1
  }

  test("自定义 Promise") {
    var ret = 0
    pipeline.addLast(new Handler {
      override def received(ctx: HandlerContext, msg: Object): Unit = {
        ctx.sendReceived(msg)
      }

      override def write(ctx: HandlerContext, msg: Object, promise: Promise[Int]): Unit = {
        ret += 1
      }
    })
    pipeline.addLast(new Handler {
      override def received(ctx: HandlerContext, msg: Object): Unit = {
        val promise = Promise[Int]()
        ctx.write(msg, promise)
      }
    })

    pipeline.sendReceived(null)

    ret shouldEqual 1
  }

}
