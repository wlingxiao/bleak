package bleak

import bleak.logging.Logging
import bleak.matcher.PathMatcher

import scala.concurrent.Future

trait Application extends Router with Logging {
  private[this] var globalFilter: Filter[Context, Context, Context, Context] =
    Filter.identity

  protected def received(
      ctx: Context,
      service: Service[Context, Context]
  ): Future[Context] =
    globalFilter.apply(ctx, service)

  def sessionManager: SessionManager

  def pathMatcher: PathMatcher

  def host: String

  def port: Int

  def use(middleware: Filter[Context, Context, Context, Context]): this.type = {
    globalFilter = globalFilter.andThen(middleware)
    this
  }

  def use(router: Router): this.type = {
    router.routes.foreach(addRoute)
    this
  }

  def run(host: String, port: Int): Unit
  def run(): Unit

  def start(host: String, port: Int): Unit
  def start(): Unit

  def stop(): Unit
}
