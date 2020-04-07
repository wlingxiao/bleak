package bleak

import java.nio.charset.StandardCharsets

import io.netty.handler.codec.http.HttpMethod

import scala.concurrent.Future
import Executions.directEc
import bleak.Content.ByteBufContent

class EmbeddedClientSpec extends Spec {

  "EmbeddedClient.fetch" should {
    "fetch data from server" in {
      val app = Bleak()
      app.get("/hello")(Future(Response(content = "world")))
      val client = new EmbeddedClient(app)
      client
        .fetch(HttpMethod.GET, "/hello")
        .content
        .asInstanceOf[ByteBufContent]
        .buf
        .toString(StandardCharsets.UTF_8) should_== "world"
    }
  }

}
