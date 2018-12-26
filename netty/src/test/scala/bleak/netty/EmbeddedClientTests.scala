package bleak.netty

import bleak._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class EmbeddedClientTests extends FunSuite with Matchers with BeforeAndAfter with MockitoSugar {

  test("perform simple get request") {
    val app: Netty = new Netty {}

    app.get("/test") { ctx =>
      Ok("test", headers = Map("abc" -> "123"), cookies = Seq(Cookie("xyz", "789")))
    }
    val client = new EmbeddedClient(app)
    val response = client.get("/test")

    response.status shouldEqual Status.Ok
    response.headers.get("abc") shouldEqual Some("123")
    response.cookies("xyz").value shouldEqual Some("789")
  }

  test("perform full get request") {
    val app: Netty = new Netty {}

    app.get("/test") { ctx =>
      val params = ctx.request.params

      params.get("hello") shouldEqual Some("world")
      params.get("hello2") shouldEqual Some("world2")

      val headers = ctx.request.headers
      headers.get("header1") shouldEqual Option("value1")
      headers.get("header2") shouldEqual Option("value2")

      val cookies = ctx.request.cookies
      cookies("cookie1").value shouldEqual Some("value1")
      cookies("cookie2").value shouldEqual Some("value2")

      Ok("test", headers = Map("abc" -> "123"), cookies = Seq(Cookie("xyz", "789")))
    }

    val client = new EmbeddedClient(app)

    val params = Map("hello" -> "world", "hello2" -> "world2")
    val headers = Map("header1" -> "value1", "header2" -> "value2")
    val cookies = Seq(Cookie("cookie1", "value1"), Cookie("cookie2", "value2"))
    val response = client.get("/test", params = params, headers = headers, cookies = cookies)

    response.status shouldEqual Status.Ok
    response.headers.get("abc") shouldEqual Some("123")
    response.cookies("xyz").value shouldEqual Some("789")
  }

  test("perform simple post request") {
    val app: Netty = new Netty {}

    app.post("/test") { ctx =>
      ctx.request.body.string shouldEqual "name=111"

      Ok("test", headers = Map("abc" -> "123"), cookies = Seq(Cookie("xyz", "789")))
    }

    val client = new EmbeddedClient(app)
    val response = client.post("/test", data = Map("name" -> 111))

    response.status shouldEqual Status.Ok
    response.headers.get("abc") shouldEqual Some("123")
    response.cookies("xyz").value shouldEqual Some("789")
  }
}
