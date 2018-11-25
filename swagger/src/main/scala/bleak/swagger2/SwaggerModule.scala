package bleak.swagger2

import bleak.{App, Module}

class SwaggerModule(apiConfig: ApiConfig) extends Module {

  override def init(app: App): Unit = {
    app.mount(new SwaggerController(app))
  }

}
