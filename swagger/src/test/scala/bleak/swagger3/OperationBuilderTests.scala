package bleak
package swagger3

import io.swagger.v3.oas.models.{Components, OpenAPI}

class OperationBuilderTests extends BaseTest {

  test("build swagger operation") {
    val builder = OperationBuilder(
      summary = "hello",
      desc = "world",
      tags = Seq("foo"),
      id = "bar",
      deprecated = false)

    val openAPI = new OpenAPI
    val components = new Components
    openAPI.setComponents(components)
    val op = builder.build(openAPI)

    op.getSummary shouldEqual "hello"
    op.getDescription shouldEqual "world"
    op.getTags.get(0) shouldEqual "foo"
    op.getDeprecated shouldBe null

    val op1 = builder.copy(deprecated = true).build(openAPI)
    op1.getDeprecated shouldBe true

  }

}
