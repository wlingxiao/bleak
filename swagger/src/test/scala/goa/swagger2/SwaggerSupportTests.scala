package goa.swagger2

import goa._
import io.swagger.annotations.ApiModel
import io.swagger.models.{ArrayModel, ModelImpl, RefModel, Swagger}
import io.swagger.models.parameters._
import io.swagger.models.properties.RefProperty
import org.mockito.Mockito
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.collection.mutable.ArrayBuffer

@ApiModel(description = "body param test")
case class BodyParamTest(@ApiModelProperty(value = "name") name: String,
                         @ApiModelProperty(value = "age") age: Int)

@ApiModel(description = "model test")
case class ModelTest(@ApiModelProperty(value = "field1") field1: String,
                     @ApiModelProperty(value = "field2") field2: Long)

class SwaggerSupportTests extends FunSuite with Matchers with BeforeAndAfter {

  private val api = Api(produces = "application/json", tags = Seq("用户"))

  private val apiConfig = ApiConfig(basePath = "")

  test("to swagger") {
    val swaggerApi = new SwaggerApi(api, "route1", apiConfig)
    val app = Mockito.mock(classOf[goa.App])
    val route1 = Route("/route1", Method.Get, "route1", Map.empty)
    Mockito.when(app.routes).thenReturn(ArrayBuffer(route1))
    swaggerApi
      .operation[Long](summary = "summary", notes = "notes")
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

  test("test swagger model") {
    val app = Mockito.mock(classOf[goa.App])
    val routes = ArrayBuffer(
      Route("/object", Method.Get, "object", Map.empty),
      Route("/collection", Method.Get, "collection", Map.empty),
      Route("/baseType", Method.Get, "baseType", Map.empty)
    )
    Mockito.when(app.routes).thenReturn(routes)

    val objectModel = new SwaggerApi(api, "object", apiConfig)
    objectModel
      .operation[ModelTest](summary = "object summary", notes = "object notes")

    val collectionModel = new SwaggerApi(api, "collection", apiConfig)
    collectionModel
      .operation[List[ModelTest]]("swagger api 2 summary")

    val baseTypeModel = new SwaggerApi(api, "baseType", apiConfig)
    baseTypeModel.operation[Long]("baseType summary")

    val swagger = new Swagger()
    objectModel.toSwagger(swagger, app)
    collectionModel.toSwagger(swagger, app)
    baseTypeModel.toSwagger(swagger, app)

    {
      val get = swagger.getPath("/object").getGet
      val response = get.getResponses.get("200")
      val model = response.getResponseSchema.asInstanceOf[RefModel]
      model.getSimpleRef shouldEqual "ModelTest"
    }

    {
      val get = swagger.getPath("/collection").getGet
      val response = get.getResponses.get("200")
      val model = response.getResponseSchema.asInstanceOf[ArrayModel]
      val refProperty = model.getItems.asInstanceOf[RefProperty]
      refProperty.getSimpleRef shouldEqual "ModelTest"
    }

    {
      val get = swagger.getPath("/baseType").getGet
      val response = get.getResponses.get("200")
      val model = response.getResponseSchema.asInstanceOf[ModelImpl]
      model.getType shouldEqual "integer"
      model.getFormat shouldEqual "int64"
    }

  }

}
