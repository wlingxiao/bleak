package goa.swagger2

import goa.{App, Module}

class SwaggerModule(apiConfig: ApiConfig) extends Module {

  override def init(app: App): Unit = {
    app.mount(swaggerController)
  }

  protected def swaggerController: SwaggerController = {
    new SwaggerController
  }

}
