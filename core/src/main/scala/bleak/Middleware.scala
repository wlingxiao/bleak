package bleak

import scala.concurrent.Future

trait Middleware {
  def apply(ctx: Context, request: Request): Future[Response]
}
