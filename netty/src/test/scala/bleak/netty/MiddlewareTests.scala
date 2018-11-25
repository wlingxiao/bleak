package bleak.netty

import bleak._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.concurrent.Future

class MiddlewareTests extends FunSuite with Matchers with BeforeAndAfter with MockitoSugar {

  private val mockResponseFuture = Future.successful[Context](null)

  val sessionManager = mock[SessionManager]

  val request = mock[Request]

  val response = mock[Response]

  var pipeline: DefaultPipeline = _

  before {
    pipeline = DefaultPipeline(sessionManager)
  }

  test("append Middleware") {
    var i = 0
    pipeline.append { ctx =>
      i += 1
      ctx.next()
    }.append { ctx =>
      i += 1
      mockResponseFuture
    }

    received()

    i shouldEqual 2

  }

  test("insert Middleware") {
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

    received()

    i shouldEqual 0

  }

  test("addBefore Middleware") {
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

    received()

    i shouldEqual 0
  }

  private def received(): Unit = {
    pipeline.received(request, response)
  }


}
