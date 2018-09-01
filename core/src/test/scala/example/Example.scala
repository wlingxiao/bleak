package example

import goa._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Success

class Greeting extends Controller {

  get("/hello") { ctx =>
    ctx.ok()
  }

  post("/hello") { ctx =>
    ctx.ok()
  }

}


import scala.concurrent.ExecutionContext.Implicits.global

object Example {
  def main(args: Array[String]): Unit = {
    val app = Goa()
    app.mount(new Greeting)
    app.run()
  }
}
