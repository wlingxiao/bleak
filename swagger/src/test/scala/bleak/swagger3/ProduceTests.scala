package bleak
package swagger3

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.MapSchema
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class ProduceTests extends BaseTest {

  import ProduceTests._

  test("build ApiResponse") {
    val components = new Components
    val res = new Produce[UserArray](mimeType = MimeType.Json)
    val apiRes = res.build(components)

    val content = apiRes.getContent
    val contentSchema = content.get(MimeType.Json).getSchema
    contentSchema.get$ref() shouldEqual "#/components/schemas/" + res.schemaName

    val s = components.getSchemas.get(res.schemaName)
    val extrasSchema = s.getProperties.get("extras")
    extrasSchema.isInstanceOf[MapSchema] shouldBe true
    extrasSchema.getProperties shouldBe null
    extrasSchema.getAdditionalProperties shouldNot be(null)
  }

  test("build ApiResponse for noting") {
    val components = new Components
    val res = Produce()
    val apiRes = res.build(components)
    apiRes.getContent.size() shouldEqual 0
  }

  test("build ApiResponse for string") {
    val components = new Components
    val res = Produce[String](mimeType = MimeType.Json)
    val apiRes = res.build(components)
    apiRes.getContent.get(MimeType.Json).getSchema.getType shouldEqual "string"
  }

}

object ProduceTests {

  case class UserArray(extras: Map[String, Any])

}