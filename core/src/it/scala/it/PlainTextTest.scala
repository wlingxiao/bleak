package it

import com.mashape.unirest.http.Unirest
import goa._

class PlainTextTest extends IntegrationTest {

  var app: Goa = _

  before {

  }

  test("plain text response") {
    app = Goa()
    app.get("/plain") {
      response.contentType = MediaType.PlainText
      "plain"
    }

    app.start()

    val ret = Unirest
      .get("http://localhost:7865/plain")
      .header("Connection", "close")
      .asString().getBody

    ret shouldEqual "plain"
  }

  after {
    app.stop()
  }

}
