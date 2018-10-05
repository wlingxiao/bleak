package goa.swagger2

import goa._
import io.swagger.annotations.ApiModel
import io.swagger.models.Swagger
import io.swagger.models.parameters._
import org.mockito.Mockito
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

@ApiModel(description = "body param test")
case class BodyParamTest(@ApiModelProperty(value = "name") name: String,
                         @ApiModelProperty(value = "age") age: Int)

class SwaggerSupportTests extends FunSuite with Matchers with BeforeAndAfter {

  private val api = Api(produces = "application/json", tags = Seq("用户"))

  private val apiConfig = ApiConfig(basePath = "")

  test("to swagger") {
    val swaggerApi = new SwaggerApi(api, "route1", apiConfig)
    val app = Mockito.mock(classOf[goa.App])
    Mockito.when(app.routers).thenReturn(List(Route("/route1", Seq(Method.Get)).name("route1")))
    swaggerApi
      .operation(summary = "summary", notes = "notes")
      .query[String]("query param", "query param desc")
      .path[Long]("path param", "path param desc")
      .body[BodyParamTest](desc = "body param desc")
      .cookie[Long]("cookie param", "cookie param desc")
      .header[Long]("header param", "header param desc")
      .form[Long]("form param", "form param desc")
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

    val body = getOperation.getParameters.get(2).asInstanceOf[BodyParameter]
    body.getDescription shouldEqual "body param desc"
    body.getSchema.getProperties.size() shouldEqual 2

    val cookie = getOperation.getParameters.get(3).asInstanceOf[CookieParameter]
    cookie.getName shouldEqual "cookie param"
    cookie.getDescription shouldEqual "cookie param desc"
    cookie.getType shouldEqual "integer"
    cookie.getFormat shouldEqual "int64"

    val header = getOperation.getParameters.get(4).asInstanceOf[HeaderParameter]
    header.getName shouldEqual "header param"
    header.getDescription shouldEqual "header param desc"
    header.getType shouldEqual "integer"
    header.getFormat shouldEqual "int64"

    val form = getOperation.getParameters.get(5).asInstanceOf[FormParameter]
    form.getName shouldEqual "form param"
    form.getDescription shouldEqual "form param desc"
    form.getType shouldEqual "integer"
    form.getFormat shouldEqual "int64"
  }

}
