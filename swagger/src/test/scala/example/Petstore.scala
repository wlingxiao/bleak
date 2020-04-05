package example

import bleak.swagger3.{Api, SwaggerUIRouter}
import bleak.{AccessLogMiddleware, Bleak}
import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.info.{Contact, Info, License}
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag

object Petstore {

  def main(args: Array[String]): Unit = {
    val app = Bleak()
    configSwagger()
    doc()
    app.use(new AccessLogMiddleware)
    app.use(new SwaggerUIRouter())
    app.start()
  }

  def configSwagger(): Unit = {
    val info = new Info()
      .title("Swagger Petstore")
      .description(
        "This is a sample Petstore server.  You can find\nout more about Swagger at\n[http://swagger.io](http://swagger.io) or on\n[irc.freenode.net, #swagger](http://swagger.io/irc/).\n")
      .termsOfService("http://swagger.io/terms/")
      .contact(new Contact().email("apiteam@swagger.io"))
      .license(new License()
        .name("Apache 2.0")
        .url("http://www.apache.org/licenses/LICENSE-2.0.html"))
      .version("1.0.0")

    val servers = Seq(
      new Server()
        .url("https://virtserver.swaggerhub.com/wlingxiao/test/1.0.0")
        .description("SwaggerHub API Auto Mocking"),
      new Server().url("https://petstore.swagger.io/v2")
    )

    val tags =
      Seq(
        new Tag()
          .name("pet")
          .description("Everything about your Pets")
          .externalDocs(
            new ExternalDocumentation()
              .description("Find out more")
              .url("http://swagger.io")),
        new Tag()
          .name("store")
          .description("Access to Petstore orders")
      )

    Api.config(info, tags, servers)
  }

  def doc(): Unit = {
    Api
      .doc("PUT", "/pet", "Update an existing pet", tags = Seq("pet"))
      .requestBody[Pet](
        "Pet object that needs to be added to the store",
        Seq("application/json", "application/xml"))
      .response[Nothing]("400", desc = "Invalid ID supplied")
      .response[Nothing]("404", desc = "Pet not found")
      .response[Nothing]("405", desc = "Validation exception")

    Api
      .doc("POST", "/store/order", "Place an order for a pet", tags = Seq("store"))
      .requestBody[Order]("order placed for purchasing the pet", Seq("application/json"))
      .response[Order]("200", "successful operation", Seq("application/json", "application/xml"))
      .response[Nothing]("400", "Invalid Order")
  }

}
