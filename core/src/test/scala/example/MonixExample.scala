package example

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object MonixExample extends App {

  def fnOne(): Future[Long] = {
    Future.successful(100)
  }

  def fnTow: Future[Long] = {
    Future {
      println("how are you")
      100
    }
  }

  fnTow


  Thread.sleep(1000)
}
