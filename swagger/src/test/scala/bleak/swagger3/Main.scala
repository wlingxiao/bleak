package bleak.swagger3

import java.net.URL

import bleak.{AccessLogMiddleware, Bleak}

object Main extends App {

  val app = Bleak()

  app.use(new AccessLogMiddleware)
  app.use(new SwaggerRouter())

  app.start()

}
