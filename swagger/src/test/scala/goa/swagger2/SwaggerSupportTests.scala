package goa.swagger2

import goa._
import io.swagger.models.Swagger
import io.swagger.models.parameters.{PathParameter, QueryParameter}
import org.mockito.Mockito
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class SwaggerSupportTests extends FunSuite with Matchers with BeforeAndAfter {

  private val api = Api(produces = "application/json", tags = Seq("用户"))

  private val apiConfig = ApiConfig(basePath = "")

  test("to swagger") {
    val swaggerApi = new SwaggerApi(api, "route1", apiConfig)
    val app = Mockito.mock(classOf[goa.App])
    Mockito.when(app.routers).thenReturn(List(Route("/route1", Seq(Method.Get)).name("route1")))
    swaggerApi
      .operation(summary = "summary", notes = "notes")
      .param(QueryParam[String]("query param", "query param desc"))
      .param(PathParam[Long]("path param", "path param desc"))
    val swagger = new Swagger()

    swaggerApi.toSwagger(swagger, app)

    val getOperation = swagger.getPaths.get("/route1").getGet
    getOperation.getSummary shouldEqual "summary"
    getOperation.getDescription shouldEqual "notes"

    val query = getOperation.getParameters.get(0).asInstanceOf[QueryParameter]
    query.getIn shouldEqual "query"
    query.getName shouldEqual "query param"
    query.getDescription shouldEqual "query param desc"
    query.getType shouldEqual "string"
    query.getFormat shouldBe null

    val path = getOperation.getParameters.get(1).asInstanceOf[PathParameter]
    path.getIn shouldEqual "path"
    path.getName shouldEqual "path param"
    path.getDescription shouldEqual "path param desc"
    path.getDescription shouldEqual "path param desc"
    path.getType shouldEqual "integer"
    path.getFormat shouldEqual "int64"

  }

}
