package it

import com.mashape.unirest.http.Unirest
import goa._
import goa.annotation._

private case class UserQueryParam(name: String, age: Long)

@route("/users")
private class UserController {

  @get("/{id}")
  def getUserById(@path id: Long, name: String): Long = {
    id
  }

  @get("/list/{id}")
  def listUsers(@path id: Long, @query name: String, @header username: String, @cookie auth: String): String = {
    id + name + username + auth
  }

  @get
  def searchUser(@query param: UserQueryParam): String = {
    param.age + param.name
  }

  @post
  def createUser(@body param: UserQueryParam): String = {
    param.name + param.age
  }

  @post("/primitive")
  def createPrimitive(@body name: String, @body age: Long): String = {
    name + age + "primitive"
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

  test("list users") {
    app.mount(new UserController)
    app.start()
    val ret = Unirest
      .get("http://localhost:7865/users/list/666")
      .header("Connection", "close")
      .queryString("name", "admin")
      .header("username", "hello")
      .header("Cookie", "auth=test")
      .asString().getBody
    ret shouldEqual "666adminhellotest"
  }

  test("search users") {
    app.mount(new UserController)
    app.start()
    val ret = Unirest
      .get("http://localhost:7865/users")
      .header("Connection", "close")
      .queryString("name", "admin")
      .queryString("age", 100)
      .asString().getBody
    ret shouldEqual "100admin"
  }

  test("create user") {
    app.mount(new UserController)
    app.start()
    val ret = Unirest
      .post("http://localhost:7865/users")
      .header("Connection", "close")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .body("name=admin&age=100")
      .asString().getBody
    ret shouldEqual "admin100"
  }

  test("create primitive") {
    app.mount(new UserController)
    app.start()
    val ret = Unirest
      .post("http://localhost:7865/users/primitive")
      .header("Connection", "close")
      .header("Content-Type", "application/x-www-form-urlencoded")
      .body("name=admin&age=100")
      .asString().getBody
    ret shouldEqual "admin100primitive"
  }


  after {
    app.stop()
  }

}
