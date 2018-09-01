package goa.exp

import goa.channel.HandlerContext
import org.mockito.Mockito

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success}

object Abc extends App {

  val chain = new MiddlewareChain
  chain.addLast((ctx: Context) => {
    ctx.next().map { x =>
      println(ctx.request.path)
      x
    }
  })

  chain.addLast { ctx =>
    Future {
      100

    }
  }

  val request = Mockito.mock(classOf[goa.Request])
  val context = Mockito.mock(classOf[HandlerContext])
  Mockito.when(request.path).thenReturn("/test")
  chain.messageReceived(request, context).onComplete {
    case Success(value) => println(value)
    case Failure(cause) => println(cause)
  }

  Thread.sleep(1000)
}
