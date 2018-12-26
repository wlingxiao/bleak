package bleak

import scala.concurrent.Future

trait Middleware extends (Context => Future[Context]) {
  override def apply(ctx: Context): Future[Context]
}