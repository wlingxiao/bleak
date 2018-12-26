package bleak.netty

import fetches.{fetches, Cookie}
import bleak._
import org.scalatest.{AsyncFunSuite, BeforeAndAfterAll}

class NettyIntegrationTests extends AsyncFunSuite with BeforeAndAfterAll {

  private val host = "http://127.0.0.1:7865"

  private var app: Application = _

  override protected def beforeAll(): Unit = {
    app = new Netty
    app.get("/get")("get")
    app.post("/post")("post")
    app.post("/params/{id}") { ctx =>
      val id = ctx.params.get("id").get
      val hello = ctx.params.get("hello").get
      val lol = ctx.params.get("lol").get
      id + hello + lol
    }
    app.get("/headers") { ctx =>
      ctx.header("hello").get
    }
    app.get("/cookies") { ctx =>
      ctx.request.cookies("hello").value
    }
    app.start()
  }

  test("make get request") {
    fetches.get(host + "/get") flatMap { res =>
      assert(res.text == "get")
      fetches.post(host + "/get")
    } map { res =>
      res.okResponse.close()
      assert(res.code == Status.MethodNotAllowed.code)
    }
  }

  test("make post request") {
    fetches.post(host + "/post") flatMap { res =>
      assert(res.text == "post")
      fetches.get(host + "/post")
    } map { res =>
      res.okResponse.close()
      assert(res.code == Status.MethodNotAllowed.code)
    }
  }

  test("get params from path, uri and request body") {
    fetches.post(host + "/params/123", params = Map("hello" -> "world"), data = Map("lol" -> "haha")) flatMap { res =>
      assert(res.text == "123worldhaha")
    }
  }

  test("get headers") {
    fetches.get(host + "/headers", headers = Map("hello" -> "world")) flatMap { res =>
      assert(res.text == "world")
    }
  }

  test("get cookies") {
    val cookie = Cookie("hello", "world", "127.0.0.1")
    fetches.get(host + "/cookies", cookies = List(cookie)) flatMap { res =>
      assert(res.text == "world")
    }
  }

  test("store session") {
    val cookie = Cookie("hello", "world", "127.0.0.1")
    fetches.get(host + "/cookies", cookies = List(cookie)) flatMap { res =>
      assert(res.text == "world")
    }
  }

  override protected def afterAll(): Unit = {
    app.stop()
  }
}
