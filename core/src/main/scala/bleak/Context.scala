package bleak

trait Context {

  def request: Request

  def request(req: Request): Context

  def response: Response

  def response(res: Response): Context

  def session: Option[Session]

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

trait HttpContext extends Context {
  override def request(req: Request): HttpContext
  override def response(resp: Response): HttpContext
}

trait WebsocketContext extends Context {
  override def request(req: Request): WebsocketContext
  override def response(resp: Response): WebsocketContext
  def send(frame: WebsocketFrame): Unit
  def on(fun: PartialFunction[WebsocketFrame, Unit]): Unit
}
