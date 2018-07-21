package goa.annotation

import goa.{BaseTests, Method}

private case class PostParam(id: Long, name: String)

private case class PostBodyParam(age: Int)

@route("/users")
private class UserController {

  @get("/{id}")
  def searchUser(@path id: Long, name: String): Unit = {
    println(id + name)
  }

  @post def searchPost(param: PostParam, @body body: PostBodyParam): String = {
    param.name
  }
}

@route("/cookie-header")
private class HeaderCookieController {

  @get("/{id}") def searchUser(@header id: Long, @cookie name: String): Unit = {

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
    routeOneFirstParam.param.get.isInstanceOf[path] shouldBe true
    routeOneFirstParam.parameter.getName shouldEqual "id"

    val routeOneSecondParam = routes.head.params.tail.head
    routeOneSecondParam.param shouldEqual None // 默认为QueryParam
    routeOneSecondParam.parameter.getName shouldEqual "name"

    routeTow.path shouldEqual "/users"
    routeTow.method shouldEqual Method.Post

    val routeTowFirstParam = routeTow.params.head
    routeTowFirstParam.param shouldEqual None
    routeTowFirstParam.parameter.getName shouldEqual "param"

    val routeTowSecondParam = routeTow.params.tail.head
    routeTowSecondParam.param.get.isInstanceOf[body] shouldBe true
    routeTowSecondParam.parameter.getName shouldEqual "body"
  }

  test("test process cookie param and header param") {
    val routes = processor.process(new HeaderCookieController)
    val route = routes.head

    route.path shouldEqual "/cookie-header/{id}"
    route.method shouldEqual Method.Get

    val firstParam = route.params.head
    firstParam.param.get.isInstanceOf[header] shouldBe true
    firstParam.parameter.getName shouldEqual "id"

    val secondParam = route.params.tail.head
    secondParam.param.get.isInstanceOf[cookie] shouldBe true
    secondParam.parameter.getName shouldEqual "name"
  }

}
