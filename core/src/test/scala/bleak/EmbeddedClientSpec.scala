package bleak

import bleak.util.Executions
import io.netty.handler.codec.http.HttpMethod

import scala.concurrent.Future

class EmbeddedClientSpec extends Spec {

  "EmbeddedClient.fetch" should {
    "fetch data from server" in {
      val app = Bleak()
      app.get("/hello")(Future(Response(content = "world"))(Executions.directec))
      val client = new EmbeddedClient(app)
      client.fetch(HttpMethod.GET, "/hello").content.text should_== "world"
    }
  }

}
