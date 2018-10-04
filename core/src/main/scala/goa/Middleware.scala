package goa

import java.util.concurrent.atomic.AtomicReference

import scala.concurrent.Future

trait Middleware extends (Context => Future[Response]) {

  override def apply(ctx: Context): Future[Response]

}

trait Context extends ResponseBuilder {

  def name: String

  def request: Request

  def request(value: Request): this.type

  def next(): Future[Response]

}

trait Pipeline {


  /**
    * Append [[Middleware]]s at the last position of this pipeline
    *
    * @param middlewares the middlewares to insert last
    */
  def append(middlewares: Middleware*): this.type

  /**
    * Append a [[Middleware]] at the last position of this pipeline
    *
    * @param name       the name of the middleware to append
    * @param middleware the middleware to append
    */
  def append(name: String)(middleware: Middleware): this.type

  /**
    * Append [[Middleware]]s at the first position of this pipeline
    *
    * @param middlewares the middlewares to insert first
    */
  def prepend(middlewares: Middleware*): this.type

  /**
    * Insert a [[Middleware]] at the first position of this pipeline
    *
    * @param name       the name of the middleware to insert first
    * @param middleware the middleware to insert first
    */
  def prepend(name: String)(middleware: Middleware): this.type

  /**
    * Insert a [[Middleware]] after an existing middleware of this pipeline.
    *
    * @param baseName   the name of the existing middleware
    * @param name       the name of the middleware to insert after
    * @param middleware the middleware to insert after
    */
  def insert(baseName: String)(name: String, middleware: Middleware): this.type

  def received(request: Request): Future[Response]
}

object Pipeline {

  def apply(): Pipeline = new Impl

  private class DefaultMiddleware extends Middleware {
    override def apply(ctx: Context): Future[Response] = ctx.next()
  }


  private class DefaultContext(var prevCtx: DefaultContext,
                               var nextCtx: DefaultContext,
                               val middleware: Middleware,
                               var _name: String,
                               val pipeline: Pipeline) extends Context {

    private val requestRef = new AtomicReference[Request]()

    def name: String = _name

    override def request: Request = requestRef.get()

    override def request(value: Request): this.type = {
      requestRef.set(value)
      this
    }

    override def next(): Future[Response] = {
      if (nextCtx != null) {
        nextCtx.request(request).middleware.apply(nextCtx)
      } else null
    }
  }

  private[goa] class Impl extends Pipeline {
    private val head = new DefaultContext(null, null, new DefaultMiddleware(), "HEAD", this)

    private val tail = new DefaultContext(head, null, new DefaultMiddleware(), "TAIL", this)

    head.nextCtx = tail
    tail.prevCtx = head

    override def append(middlewares: Middleware*): this.type = {
      middlewares.foreach(addLast(null, _))
      this
    }

    override def append(name: String)(middleware: Middleware): this.type = {
      addLast(name, middleware)
    }

    override def prepend(middlewares: Middleware*): this.type = {
      middlewares.foreach(addFirst(null, _))
      this
    }

    override def prepend(name: String)(middleware: Middleware): this.type = {
      addFirst(name, middleware)
    }

    def addAfter(baseName: String, name: String, middleware: Middleware): this.type = {
      val ctx = findContext(baseName)
      val newCtx = newContext(name, middleware)
      newCtx.prevCtx = ctx
      newCtx.nextCtx = ctx.nextCtx
      ctx.nextCtx.prevCtx = newCtx
      ctx.nextCtx = newCtx
      this
    }

    private def findContext(name: String): DefaultContext = {
      var ctx = head.nextCtx
      while (ctx != tail) {
        if (ctx.name.equals(name)) {
          return ctx
        }
        ctx = ctx.nextCtx
      }
      null
    }

    def addBefore(baseName: String, name: String, middleware: Middleware): this.type = {
      val ctx = findContext(baseName)
      val newCtx = newContext(name, middleware)
      newCtx.prevCtx = ctx.prevCtx
      newCtx.nextCtx = ctx
      ctx.prevCtx.nextCtx = newCtx
      ctx.prevCtx = newCtx
      this
    }

    def addFirst(name: String, middleware: Middleware): this.type = {
      val newCtx = newContext(name, middleware)
      val nextCtx = head.nextCtx
      newCtx.prevCtx = head
      newCtx.nextCtx = nextCtx
      head.nextCtx = newCtx
      nextCtx.prevCtx = newCtx
      this
    }

    def addLast(name: String, middleware: Middleware): this.type = {
      val prev = tail.prevCtx
      val newCtx = newContext(name, middleware)
      newCtx.prevCtx = prev
      newCtx.nextCtx = tail
      prev.nextCtx = newCtx
      tail.prevCtx = newCtx
      this
    }

    private def newContext(name: String, middleware: Middleware): DefaultContext = {
      new DefaultContext(null, null, middleware, name, this)
    }

    override def insert(baseName: String)(name: String, middleware: Middleware): this.type = {
      addAfter(baseName, name, middleware)
    }

    override def received(request: Request): Future[Response] = {
      head.request(request).middleware.apply(head)
    }
  }

}
