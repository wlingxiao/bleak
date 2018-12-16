package bleak.swagger2

import bleak.{Application, Module}

class SwaggerModule(apiConfig: ApiConfig) extends Module {

  override def init(app: Application): Unit = {
    app.mount(new SwaggerController(app))
  }

}
