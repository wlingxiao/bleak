package goa.annotation

import goa.{BaseTests, Method}

private case class PostParam(id: Long, name: String)

import scala.reflect.runtime.universe._

private case class PostBodyParam(age: Int)

@Path("/users")
private class UserController {

  @GET("/{id}")
  def searchUser(@PathParam id: Long, name: String): Unit = {
    println(id + name)
  }

  @POST def searchPost(param: PostParam, @Body body: PostBodyParam): String = {
    param.name
  }
}

@Path("/cookie-header")
private class HeaderCookieController {

  @GET("/{id}") def searchUser(@header id: Long, @cookie name: String): Unit = {

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
    routeTowFirstParam.param shouldEqual Some(QueryParam("param"))
    routeTowFirstParam.symbol.info =:= typeOf[PostParam] shouldBe true

    val routeTowSecondParam = routeTow.params.tail.head
    routeTowSecondParam.param shouldEqual Some(Body("body"))
    routeTowSecondParam.symbol.info =:= typeOf[PostBodyParam] shouldBe true
  }

  test("test process cookie param and header param") {
    val routes = processor.process(new HeaderCookieController)
    val route = routes.head

    route.path shouldEqual "/cookie-header/{id}"
    route.method shouldEqual Method.Get

    val firstParam = route.params.head
    firstParam.param shouldEqual Some(header("id"))
    firstParam.symbol.info =:= typeOf[Long] shouldBe true

    val secondParam = route.params.tail.head
    secondParam.param shouldEqual Some(cookie("name"))
    secondParam.symbol.info =:= typeOf[String] shouldBe true
  }

}
