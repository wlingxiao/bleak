package goa.swagger

import io.swagger.config.{ScannerFactory, SwaggerConfig}
import io.swagger.models.Swagger

object ApiListingCache {

  def listing(docRoot: String, host: String): Option[Swagger] = {
    val scanner = ScannerFactory.getScanner
    val classes = scanner.classes()
    val reader = new GoaReader(null)
    var swagger = reader.read(classes)

    scanner match {
      case config: SwaggerConfig =>
        swagger = config.configure(swagger)
      case config =>
    }
    Some(swagger)
  }
}
