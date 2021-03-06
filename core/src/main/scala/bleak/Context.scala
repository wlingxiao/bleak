package bleak

import scala.concurrent.Future

trait Context {

  def request: Request

  def request_=(req: Request): Unit

  def response: Response

  def response_=(resp: Response): Unit

  def next(): Future[Context]

  def session: Option[Session]

  def header(name: String): Option[String] = {
    request.headers.get(name)
  }

  def method: Method = request.method

  def uri: String = request.uri

  def path: String = request.path

  def params: Params[String] = request.params

  def userAgent: Option[String] = request.userAgent

  def version: Version = request.version

  def status: Status = response.status

  def app: Application

}

trait WebSocketContext extends Context {

  def send(frame: Frame): Unit

  def on(fun: PartialFunction[Frame, Unit]): Unit

}
