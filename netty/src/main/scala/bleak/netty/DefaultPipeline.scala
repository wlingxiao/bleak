package bleak
package netty

import scala.concurrent.Future

private[netty] class DefaultPipeline(sessionManager: SessionManager, val app: Application) {

  import DefaultPipeline._

  private val head = new DefaultContext(null, null, new DefaultMiddleware(), this, null, null)

  private val tail = new DefaultContext(head, null, new DefaultMiddleware(), this, null, null)

  head.successor = tail
  tail.predecessor = head

  def append(middlewares: Middleware*): this.type = {
    middlewares.foreach { middleware =>
      val name = middleware.getClass.getSimpleName
      addLast(name, middleware)
    }
    this
  }

  def addLast(name: String, middleware: Middleware): this.type = {
    val prev = tail.predecessor
    val newCtx = newContext(name, middleware)
    newCtx.predecessor = prev
    newCtx.successor = tail
    prev.successor = newCtx
    tail.predecessor = newCtx
    this
  }

  private def newContext(name: String, middleware: Middleware): DefaultContext = {
    new DefaultContext(null, null, middleware, this, null, null)
  }

  def received(request: Request, response: Response): Future[Context] = {
    head.message(request, response).received()
  }

  def session(ctx: DefaultContext): Option[Session] = {
    sessionManager.session(ctx)
  }

}

private object DefaultPipeline {

  def apply(sessionManager: SessionManager, app: Application): DefaultPipeline = new DefaultPipeline(sessionManager, app)

  private class DefaultMiddleware extends Middleware {
    override def apply(ctx: Context): Future[Context] = ctx.next()
  }

  private class DefaultContext(var predecessor: DefaultContext,
                               var successor: DefaultContext,
                               val middleware: Middleware,
                               val pipeline: DefaultPipeline,
                               val request: Request,
                               val response: Response) extends Context {


    override def app: Application = {
      pipeline.app
    }

    override def session: Option[Session] = pipeline.session(this)

    override def request_=(req: Request): Unit = {
      copy(req = req)
    }

    override def response_=(resp: Response): Unit = {
      copy(resp = resp)
    }

    def message(req: Request, resp: Response): DefaultContext = {
      copy(req = req, resp = resp)
    }

    private def copy(req: Request = request, resp: Response = response): DefaultContext = {
      new DefaultContext(predecessor, successor, middleware, pipeline, req, resp)
    }

    override def next(): Future[Context] = {
      if (successor != null) {
        successor.message(request, response).received()
      } else Future.successful(this)
    }

    def received(): Future[Context] = {
      middleware(this)
    }
  }

}