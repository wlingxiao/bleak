package bleak
package swagger3


import io.swagger.v3.oas.models.{OpenAPI, Paths}
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class ApiTests extends FunSuite with Matchers with BeforeAndAfter {

  test("build OpenAPI") {
    val api = new Api
    val config = Config(
      info = Info(
        desc = "This is a sample Petstore server",
        version = "1.0.0",
        title = "Swagger Petstore",
        termsOfService = "http://swagger.io/terms/"),
      tags = Seq(
        Tag(name = "pet", desc = "Everything about your Pets"),
        Tag(name = "store", desc = "Access to Petstore orders"),
        Tag(name = "user", desc = "Operations about user")))

    api.doc("POST /pet")
      .operation(summary = "Add a new pet to the store", id = "addPet")
      .responses(Produce(name = "405", desc = "Invalid input"))

    val openAPI = api.build(config)
    checkInfo(openAPI)
    checkTags(openAPI)
    checkPaths(openAPI.getPaths)
  }

  private def checkInfo(openAPI: OpenAPI): Unit = {
    val info = openAPI.getInfo
    info.getDescription shouldEqual "This is a sample Petstore server"
    info.getVersion shouldEqual "1.0.0"
    info.getTitle shouldEqual "Swagger Petstore"
    info.getTermsOfService shouldEqual "http://swagger.io/terms/"
  }

  private def checkTags(openAPI: OpenAPI): Unit = {}

  private def checkPaths(paths: Paths): Unit = {
    val petPost = paths.get("/pet").getPost
    petPost.getSummary shouldEqual "Add a new pet to the store"
    petPost.getOperationId shouldEqual "addPet"
    val petPostResponses = petPost.getResponses
    petPostResponses.get("405").getDescription shouldEqual "Invalid input"
  }

}
