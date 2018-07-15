package goa.annotation

import goa.{BaseTests, Method}

private case class PostParam(id: Long, name: String)

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

  /*  @GET("")
    def searchPost(param: PostParam): String = {
      param.name
    }*/
}

class AnnotationProcessorTest extends BaseTests {

  val processor = new AnnotationProcessor

  test("test process") {
    val routes = processor.process(new UserController).sortWith((x, y) => x.path > y.path)

    routes.head.path shouldEqual "/users/{id}"
    routes.head.method shouldEqual Method.Get
    val param = routes.head.params.head
    param.param shouldEqual Some(PathParam())
    param.symbol.info.toString shouldEqual "Long"

    routes.tail.head.path shouldEqual "/users/test/{id}"
    routes.tail.head.method shouldEqual Method.Post
  }

}
