package bleak

import scala.concurrent.Future

trait Service[-Request, +Response] extends (Request => Future[Response]) {
  def apply(request: Request): Future[Response]
}

object Service {

  abstract class Proxy[-Request, +Response](
      val self: Service[Request, Response]
  ) extends Service[Request, Response] {
    def apply(request: Request): Future[Response] = self(request)
  }

}
