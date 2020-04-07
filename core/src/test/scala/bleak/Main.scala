package bleak

import scala.concurrent.Future
import Executions.directEc

object Main extends App {
  val app = Bleak()
  app.use(new AccessLogMiddleware)

  app.get("/hello") { request =>
    println(request.args.get("name"))

    Future(Response(content = "Hello"))
  }

  app.start()
}
