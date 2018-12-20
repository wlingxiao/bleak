package bleak
package swagger3

import io.swagger.v3.core.util.Json

class ApiDocsRouter(config: Config) extends Router {

  get("/api-docs") { _ =>
    Ok(Json.mapper().writeValueAsString(api.build(config)), headers = Map(Fields.ContentType -> MimeType.Json))
  }

}
