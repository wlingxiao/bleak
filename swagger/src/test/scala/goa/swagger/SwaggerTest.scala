package goa.swagger

import java.lang.reflect.Method

import goa.annotation._
import goa.{Route, response}
import io.swagger.annotations._
import io.swagger.config.ScannerFactory
import io.swagger.models.Swagger
import io.swagger.models.parameters.{BodyParameter, PathParameter, QueryParameter}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

import scala.annotation.meta.field
import scala.beans.BeanProperty

@ApiModel(description = "user param")
case class User(@BeanProperty @(ApiModelProperty@field)(value = "username") name: String,
                @BeanProperty @(ApiModelProperty@field)(value = "age") age: Long)

@Api(tags = Array("user"))
@route("/users") class UserController {

  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "success", reference = "#/definitions/User"),
    new ApiResponse(code = 400, message = "bad request")))
  @ApiOperation(value = "filter users", produces = "application/json;utf-8", response = classOf[User])
  @get def filterUsers(@query id: Long): List[User] = {
    response.contentType = "application/json;utf-8"
    List(User("admin", 10))
  }

  @ApiOperation(value = "get user by user id", produces = "application/json;utf-8", response = classOf[User])
  @get("/{id}") def getUserById(@path id: Long): User = {
    User("admin", 10)
  }

  @ApiOperation(value = "create user", produces = "application/json;utf-8")
  @post def createUser(@body user: User): Int = {
    200
  }

}

class SwaggerTest extends FunSuite with Matchers with BeforeAndAfter {

  val processor = new AnnotationProcessor

  test("test create swagger") {
    initSwagger()
    val swagger = SwaggerFactory.swagger
    testDefinitions(swagger)

    val tags = swagger.getTags
    tags.get(0).getName shouldEqual "user"

    testFilterUsers(swagger)
    testGetUserById(swagger)
    testCreateUser(swagger)
  }

  def initSwagger(): Unit = {
    val apiConfig = ApiConfig(basePath = "/api/v1")
    val routes: Map[String, Route] = processor.process(new UserController).map { x =>
      val k = x.action match {
        case sy: Method => x.target.get.getClass.getName + "$." + sy.getName
        case _ => ""
      }
      k -> x
    }.toMap
    val routeHolder = new RouteHolder(routes)
    SwaggerFactory.routes = routeHolder
    SwaggerFactory.apiConfig = apiConfig
    val apiScanner = new ApiScanner(apiConfig, routeHolder)
    ScannerFactory.setScanner(apiScanner)
  }

  def testDefinitions(swagger: Swagger): Unit = {
    val userDefinitions = swagger.getDefinitions.get("User")
    val nameProperty = userDefinitions.getProperties.get("name")
    nameProperty.getDescription shouldEqual "username"
    nameProperty.getName shouldEqual "name"
    nameProperty.getType shouldEqual "string"

    val ageProperty = userDefinitions.getProperties.get("age")
    ageProperty.getDescription shouldEqual "age"
    ageProperty.getName shouldEqual "age"
    ageProperty.getType shouldEqual "integer"
    ageProperty.getFormat shouldEqual "int64"
  }

  def testFilterUsers(swagger: Swagger): Unit = {
    val usersGet = swagger.getPaths.get("/users").getGet
    usersGet.getSummary shouldEqual "filter users"
    usersGet.getOperationId shouldEqual "filterUsers"
    val paramHead = usersGet.getParameters.get(0)
    paramHead.getIn shouldEqual "query"
    paramHead.getName shouldEqual "id"
    paramHead.asInstanceOf[QueryParameter].getType shouldEqual "integer"
  }

  def testGetUserById(swagger: Swagger): Unit = {
    val usersGet = swagger.getPaths.get("/users").getPost
    usersGet.getSummary shouldEqual "create user"
    usersGet.getOperationId shouldEqual "createUser"
    val paramHead = usersGet.getParameters.get(0).asInstanceOf[BodyParameter]
    paramHead.getIn shouldEqual "body"
    val schema = paramHead.getSchema
    schema.getDescription shouldEqual "user param"
  }

  def testCreateUser(swagger: Swagger): Unit = {
    val usersGet = swagger.getPaths.get("/users/{id}").getGet
    usersGet.getSummary shouldEqual "get user by user id"
    usersGet.getOperationId shouldEqual "getUserById"
    val paramHead = usersGet.getParameters.get(0)
    paramHead.getIn shouldEqual "path"
    paramHead.getName shouldEqual "id"
    paramHead.asInstanceOf[PathParameter].getType shouldEqual "integer"
  }

}
