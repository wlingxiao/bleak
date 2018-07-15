package goa.annotation

import goa.{BaseTests, Method}

private case class PostParam(id: Long, name: String)

@Path("/users")
private class UserController {

  @GET("/{id}")
  def searchUser(@PathParam id: Long, name: String): Unit = {
    println(id + name)
  }

  @POST def searchPost(param: PostParam): String = {
    param.name
  }
}

class AnnotationProcessorTest extends BaseTests {

  val processor = new AnnotationProcessor

  test("test process") {
    val routes = processor.process(new UserController).sortWith((x, y) => x.path > y.path)
    val routeOne = routes.head
    val routeTow = routes(1)

    routeOne.path shouldEqual "/users/{id}"
    routeOne.method shouldEqual Method.Get
    val routeOneFirstParam = routes.head.params.head
    routeOneFirstParam.param shouldEqual Some(PathParam("id"))
    routeOneFirstParam.symbol.info.toString shouldEqual "Long"

    val routeOneSecondParam = routes.head.params.tail.head
    routeOneSecondParam.param shouldEqual Some(QueryParam("name")) // 默认为QueryParam
    routeOneSecondParam.symbol.info.toString shouldEqual "String"

    routeTow.path shouldEqual "/users"
    routeTow.method shouldEqual Method.Post

    val routeTowFirstParam = routeTow.params.head
    routeTowFirstParam.param shouldEqual Some(QueryParam("id"))
    routeTowFirstParam.symbol.info.toString shouldEqual "Long"

    val routeTowSecondParam = routeTow.params.tail.head
    routeTowSecondParam.param shouldEqual Some(QueryParam("name"))
    routeTowSecondParam.symbol.info.toString shouldEqual "String"

  }

}
