package goa.swagger2

import goa._

class SwaggerController extends Controller {

  get("/swagger-ui/**") { ctx =>
    val BasePath = "META-INF/resources/webjars/swagger-ui/2.2.10-1/"

    null
  }

  get("/api-docs") { ctx =>
    null
  }

}
