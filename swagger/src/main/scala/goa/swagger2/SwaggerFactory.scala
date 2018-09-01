package goa.swagger2

import io.swagger.models.Swagger

object SwaggerFactory {

  private[swagger2] var apiConfig: ApiConfig = _

  private[swagger2] var routes: RouteHolder = _

  def swagger: Swagger = {
    null
  }

}
