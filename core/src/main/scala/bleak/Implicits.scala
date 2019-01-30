package bleak

import bleak.util.Executions

import scala.concurrent.Future

trait Implicits {

  implicit def any2FutureResult[T](
      any: T
  )(implicit f: T => Result): Future[Result] =
    Future.successful(f(any))

  implicit def futureAny2FutureResponse[R](
      future: Future[R]
  )(implicit f: R => Result): Future[Result] =
    future.map(f(_))(Executions.directec)

}
