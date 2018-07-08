package goa.swagger

import goa.{Controller, Method, Route}
import io.swagger.annotations.{Api, ApiOperation}
import io.swagger.config.ScannerFactory
import io.swagger.models.parameters.{PathParameter, QueryParameter}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

@Api(tags = Array("6666"))
class UserController extends Controller {

  @ApiOperation(value = "/users")
  def getUsers(): Unit = {}

}

class SwaggerTest extends FunSuite with Matchers with BeforeAndAfter {

  test("create swagger") {
    ScannerFactory.setScanner(new ApiScanner())
    val swaggerConfig = new GoaSwaggerConfig()
    swaggerConfig.description = "swagger test description"
    swaggerConfig.basePath = "/api/v1"
    swaggerConfig.contact = "me"
    swaggerConfig.version = "1.0"
    swaggerConfig.title = "swagger test"
    swaggerConfig.host = "127.0.0.1"
    swaggerConfig.termsOfServiceUrl = "http://www.me.com"
    val routesRules = new java.util.HashMap[String, Route]()
    routesRules.put("goa.swagger.UserController$.getUsers", Route("/users", Method.Get, new UserController, () => ""))
    val route = new RouteWrapper(routesRules)
    RouteFactory.setRoute(route)
    GoaConfigFactory.setConfig(swaggerConfig)
    val swagger = ApiListingCache.listing("/api-docs", "127.0.0.1")
    val s = swagger.get

    s.getInfo.getDescription shouldEqual "swagger test description"
    s.getBasePath shouldEqual "/api/v1"
    s.getInfo.getContact.getName shouldEqual "me"
    s.getInfo.getVersion shouldEqual "1.0"
    s.getInfo.getTitle shouldEqual "swagger test"
    s.getHost shouldEqual "127.0.0.1"
    s.getInfo.getTermsOfService shouldEqual "http://www.me.com"

    s.getTags.get(0).getName shouldEqual "6666"

    val getPath = s.getPaths.get("/users").getGet

    getPath.getTags.get(0) shouldEqual "6666"

    getPath.getOperationId shouldEqual "getUsers"
    getPath.getParameters.get(0).asInstanceOf[QueryParameter].getType shouldEqual "string"
    getPath.getParameters.get(0).asInstanceOf[QueryParameter].getIn shouldEqual "query"
    getPath.getParameters.get(0).asInstanceOf[QueryParameter].getName shouldEqual "username"
    getPath.getParameters.get(0).asInstanceOf[QueryParameter].getRequired shouldEqual false

    getPath.getParameters.get(1).asInstanceOf[PathParameter].getType shouldEqual "integer"
    getPath.getParameters.get(1).asInstanceOf[PathParameter].getIn shouldEqual "path"
    getPath.getParameters.get(1).asInstanceOf[PathParameter].getName shouldEqual "userId"
    getPath.getParameters.get(1).asInstanceOf[PathParameter].getRequired shouldEqual true
  }

}
