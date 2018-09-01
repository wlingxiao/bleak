package goa.swagger2

import goa.{Application, Module}

class SwaggerModule(apiConfig: ApiConfig) extends Module {

  override def init(app: Application): Unit = {
    app.mount(swaggerController)
  }

  protected def swaggerController: SwaggerController = {
    new SwaggerController
  }

}
