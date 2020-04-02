package bleak

import bleak.util.Executions

import scala.concurrent.Future

object Main extends App {
  val app = Bleak()
  app.use(new AccessLogMiddleware)

  app.get("/hello") {
    Future(Response(content = "Hello"))(Executions.directec)
  }

  app.start()
}
