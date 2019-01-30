package bleak

import bleak.util.Executions

import scala.concurrent.Future

trait Implicits {

  implicit def any2FutureResponse[T](any: T)(implicit f: T => Response): Future[Response] =
    Future.successful(f(any))

  implicit def futureAny2FutureResponse[R](future: Future[R])(
      implicit f: R => Response): Future[Response] =
    future.map(f(_))(Executions.directec)

}
