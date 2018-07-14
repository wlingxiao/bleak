package goa.swagger

import io.swagger.config.ScannerFactory
import io.swagger.models.Swagger

object SwaggerFactory {

  private[swagger] var apiConfig: ApiConfig = _

  private[swagger] var routes: RouteHolder = _

  def swagger: Swagger = {
    assert(apiConfig != null)
    assert(routes != null)
    val scanner = ScannerFactory.getScanner
    val classes = scanner.classes()
    val reader = new SwaggerReader(apiConfig, routes)
    var swagger = reader.read(classes)
    swagger
  }

}
