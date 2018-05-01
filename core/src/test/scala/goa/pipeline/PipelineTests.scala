package goa.pipeline

import goa.BaseTests

import scala.concurrent.Promise

class PipelineTests extends BaseTests {

  var pipeline: Pipeline = _

  before {
    pipeline = new Pipeline
  }

  test("空 Pipeline 不会抛出异常") {
    pipeline.messageReceived(null)
  }

  test("添加两个 Handler 发送入站消息") {
    var ret = 0
    pipeline.addLast { (ctx, msg) =>
      ret += 1
      ctx.sendRead(msg)
    }

    pipeline.addLast { (ctx, msg) =>
      ret += 1
      ctx.sendRead(msg)
    }

    pipeline.messageReceived(null)

    ret shouldEqual 2
  }

  test("添加两个 Handler 发送出站消息") {
    var ret = 0
    pipeline.addLast(new Handler {
      override def received(ctx: Context, msg: Object): Unit = {
        ctx.sendRead(msg)
      }

      override def write(ctx: Context, msg: Object, promise: Promise[Int]): Unit = {
        ret += 1
      }
    })

    pipeline.addLast { (ctx, msg) =>
      ctx.write(msg)
    }

    pipeline.messageReceived(null)

    ret shouldEqual 1
  }

  test("自定义 Promise") {
    var ret = 0
    pipeline.addLast(new Handler {
      override def received(ctx: Context, msg: Object): Unit = {
        ctx.sendRead(msg)
      }

      override def write(ctx: Context, msg: Object, promise: Promise[Int]): Unit = {
        ret += 1
      }
    })

    pipeline.addLast((ctx: Context, msg: Object) => {
      val promise = Promise[Int]()
      ctx.write(msg, promise)
    })

    pipeline.messageReceived(null)

    ret shouldEqual 1
  }

}
