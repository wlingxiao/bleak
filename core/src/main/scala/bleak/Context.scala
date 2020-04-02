package bleak

import scala.concurrent.Future

trait Context {
  def next(request: Request): Future[Response]
}

object Context {
  class Impl(index: Int, middleware: IndexedSeq[Middleware]) extends Context {

    override def next(request: Request): Future[Response] = {
      assert(index < middleware.size)

      val next = new Impl(index + 1, middleware)
      middleware(index)(next, request)
    }
  }

}
