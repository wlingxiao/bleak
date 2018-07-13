package goa.annotation

import goa.{BaseTests, Method}

@Path("/users")
private class UserController {

  @GET("/{id}")
  def getUserById(@PathParam id: Long, name: String): Unit = {
    println(id + name)
  }

  @POST("/test/{id}")
  def createUser(id: Long, name: String): Unit = {
    println(name)
  }

}

class AnnotationProcessorTest extends BaseTests {

  val processor = new AnnotationProcessor

  test("test process") {
    val routes = processor.process(new UserController).sortWith((x, y) => x.path > y.path)
    routes.head.path shouldEqual "/users/{id}"
    routes.head.method shouldEqual Method.Get
    routes.head.meta(Symbol("Params")).asInstanceOf[Seq[RouteParam]].head.paramType shouldEqual Some("PathParam")
    routes.head.meta(Symbol("Params")).asInstanceOf[Seq[RouteParam]].head.name shouldEqual Some("id")
    routes.head.meta(Symbol("Params")).asInstanceOf[Seq[RouteParam]].head.info.toString shouldEqual "Long"

    routes.tail.head.path shouldEqual "/users/test/{id}"
    routes.tail.head.method shouldEqual Method.Post
  }

}
