package it


import com.mashape.unirest.http.Unirest
import goa._
import org.json.JSONObject

private case class User(name: String, age: Int)

class JsonTests extends IntegrationTest {

  var app: Goa = _

  before {
    app = Goa()
  }

  test("request") {
    app.post("/json") {
      val person = request.extract[User]
      person
    }
    app.start()

    val jsonObject = new JSONObject()
    jsonObject.put("name", "test")
    jsonObject.put("age", 10)
    val ret = Unirest
      .post("http://localhost:7865/json")
      .header("Connection", "close")
      .header("Content-Type", "application/json")
      .body(jsonObject)
      .asJson()
      .getBody

    ret.getObject.getString("name") shouldEqual "test"
    ret.getObject.getInt("age") shouldEqual 10
  }

  test("response is json object") {
    app.get("/json") {
      User("test", 10)
    }
    app.start()

    val ret = Unirest
      .get("http://localhost:7865/json")
      .header("Connection", "close")
      .asJson().getBody

    ret.getObject.getString("name") shouldEqual "test"
    ret.getObject.getInt("age") shouldEqual 10
  }


  after {
    app.stop()
  }
}
