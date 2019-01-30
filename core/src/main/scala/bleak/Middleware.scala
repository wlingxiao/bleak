package bleak

import scala.concurrent.Future

trait Middleware[I, O] extends SimpleFilter[I, O] {
  override def apply(in: I, service: Service[I, O]): Future[O]
}
