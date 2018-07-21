package goa.swagger

import java.lang.reflect.Method

import goa.annotation._
import goa.{Controller, Route}
import io.swagger.annotations.{Api, ApiOperation}
import io.swagger.config.ScannerFactory
import io.swagger.models.parameters.QueryParameter
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

@Api(tags = Array("user"))
@route("/users")
class UserController extends Controller {

  @ApiOperation(value = "get all users")
  @get
  def getUsers(id: Long): Unit = {}

}

class SwaggerTest extends FunSuite with Matchers with BeforeAndAfter {

  val processor = new AnnotationProcessor

  test("test create swagger") {
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

    val swagger = SwaggerFactory.swagger

    val tags = swagger.getTags
    tags.get(0).getName shouldEqual "user"

    val usersGet = swagger.getPaths.get("/users").getGet
    usersGet.getSummary shouldEqual "get all users"
    usersGet.getOperationId shouldEqual "getUsers"
    usersGet.getParameters.get(0).getIn shouldEqual "query"
    usersGet.getParameters.get(0).getName shouldEqual "id"
    usersGet.getParameters.get(0).asInstanceOf[QueryParameter].getType shouldEqual "integer"
  }

}
