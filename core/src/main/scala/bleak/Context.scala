package bleak

trait Context {

  def request: Request

  def response: Response

  def header(name: String): Option[String] =
    request.headers.get(name)

  def method: Method = request.method

  def uri: String = request.uri

  def path: String = request.path

  def params: Params[String] = request.params

  def userAgent: Option[String] = request.userAgent

  def version: Version = request.version

  def status: Status = response.status

  def app: Application

}

object Context {
  abstract class Proxy extends Context {
    def self: Context
    override def request: Request = self.request
    override def response: Response = self.response
    override def app: Application = self.app
  }

}

case class HttpContext(request: Request, response: Response, app: Application) extends Context

case class WebsocketContext(request: Request, response: Response, app: Application)
    extends Context {
  private[this] var _handler: PartialFunction[WebsocketFrame, Unit] = _

  def send(frame: WebsocketFrame): Unit = ???
  def on(handler: PartialFunction[WebsocketFrame, Unit]): Unit = _handler = handler
}
