package example

import goa.Goa
import goa.swagger2.{ApiConfig, SwaggerModule}

object SwaggerExample extends App {
  val app = Goa()
  app.contextPath = "/api/v1"
  app.use(new SwaggerModule(apiConfig))
  app.mount(new UserController)
  app.run()

  def apiConfig: ApiConfig = {
    ApiConfig(basePath = app.contextPath)
  }
}
