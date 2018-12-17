package bleak.swagger2

import bleak.{Application, Module}

class SwaggerModule(apiConfig: ApiConfig) extends Module {

  override def init(app: Application): Unit = {
    app.use(new SwaggerController(app))
  }

}
