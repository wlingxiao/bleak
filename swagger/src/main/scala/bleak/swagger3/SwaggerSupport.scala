package bleak.swagger3

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.swagger.v3.core.util.Json

trait SwaggerSupport {

  private lazy val _api = {
    Json.mapper().registerModule(new DefaultScalaModule())
    new Api
  }

  def api: Api = {
    _api
  }
}
