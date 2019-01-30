package bleak

import scala.concurrent.Future

case class Result(
    status: Status = Status.Ok,
    body: Buf = Buf.empty,
    headers: Iterable[(String, String)] = Iterable.empty,
    cookies: Iterable[Cookie] = Iterable.empty)

object Result {

  implicit def any2Result[T](t: T)(implicit fun: T => Buf): Result =
    Result(body = fun(t))

  implicit def result2Future(ret: Result): Future[Result] =
    Future.successful(ret)

}
