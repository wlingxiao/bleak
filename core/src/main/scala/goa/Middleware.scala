package goa

import scala.concurrent.Future

trait Middleware extends (Context => Future[Context]) {
  override def apply(ctx: Context): Future[Context]
}

trait Context {

  def name: String

  def request: Request

  def request_=(req: Request): Context = {
    request(req)
  }

  def request(req: Request): Context

  def response: Response

  def response_=(resp: Response): Context = {
    response(resp)
  }

  def response(resp: Response): Context

  def next(): Future[Context]

  def session: Option[Session]

  def header(name: String): Option[String] = {
    request.headers.get(name)
  }

  def method: Method = request.method

  def uri: String = request.uri

  def path: String = request.path

  def params: Param = request.params

  def userAgent: Option[String] = request.userAgent

  def version: Version = request.version

  def status: Status = response.status

  def status(s: Status): Context = {
    response(response.status(s))
  }

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

}