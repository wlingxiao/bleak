package goa

import scala.concurrent.Future

class MiddlewareTests extends BaseTests {

  private val mockResponseFuture = Future.successful[Response](null)

  test("append Middleware") {
    val pipeline = Pipeline()
    var i = 0
    pipeline.append { ctx =>
      i += 1
      ctx.next()
    }.append { ctx =>
      i += 1
      mockResponseFuture
    }

    pipeline.received(null)

    i shouldEqual 2

  }

  test("insert Middleware") {
    val pipeline = Pipeline()
    var i = 0

    pipeline.append("1") { ctx =>
      i += 1
      ctx.next()
    }
    pipeline.append("3") { ctx =>
      i *= 2
      ctx.next()
    }

    pipeline.insert("1")("2", (ctx: Context) => {
      i -= 1
      mockResponseFuture
    })

    pipeline.received(null)

    i shouldEqual 0

  }

  test("addBefore Middleware") {
    val pipeline = new Pipeline.Impl()
    var i = 0

    pipeline.append("middleware first") { ctx =>
      i += 1
      ctx.next()
    }

    pipeline.append("middleware three") { ctx =>
      i *= 2
      ctx.next()
    }

    pipeline.addBefore("middleware three", "middleware tow", (ctx: Context) => {
      i -= 1
      mockResponseFuture
    })

    pipeline.received(null)

    i shouldEqual 0

  }
}
