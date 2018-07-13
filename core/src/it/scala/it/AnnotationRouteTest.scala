package it

import com.mashape.unirest.http.Unirest
import goa._
import goa.annotation.{GET, Path, PathParam}

@Path("/users")
private class UserController {

  @GET("/{id}")
  def getUserById(@PathParam id: Long, name: String): Long = {
    id
  }
}

class AnnotationRouteTest extends IntegrationTest {

  var app: Goa = _

  before {
    app = Goa()
  }

  test("get user by id") {
    app.mount(new UserController)
    app.start()

    val ret = Unirest
      .get("http://localhost:7865/users/123")
      .header("Connection", "close")
      .asString().getBody
    ret shouldEqual "123"
  }


  after {
    app.stop()
  }

}
