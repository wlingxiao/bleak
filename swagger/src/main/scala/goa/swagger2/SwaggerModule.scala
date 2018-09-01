package goa.swagger2

import java.lang.reflect.Method

import goa.{Application, Module}
import io.swagger.config.ScannerFactory

class SwaggerModule(apiConfig: ApiConfig) extends Module {

  override def init(app: Application): Unit = {

  }

  protected def swaggerController: SwaggerController = {
    new SwaggerController
  }

}
