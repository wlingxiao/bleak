package bleak
package swagger3

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.ArraySchema

class ParamTests extends BaseTest {

  test("build param for array of string") {
    val components = new Components
    val param = Param[Array[String]](name = "arg1", in = Query, desc = "arg1 desc")
    val apiParam = param.build(components).head
    val schema = apiParam.getSchema.asInstanceOf[ArraySchema]
    schema.getItems.getType shouldEqual "string"
  }

  test("build param for primitive type or string") {
    val components = new Components
    val param = Param[String](name = "arg1", in = Query, desc = "arg1 desc")
    val apiParam = param.build(components)
    apiParam.head.getSchema.getType shouldEqual "string"
  }

  test("build param for noting") {
    val components = new Components
    val param = Param(name = "arg1", in = Query, desc = "arg1 desc")
    val e = intercept[IllegalArgumentException] {
      param.build(components)
    }
    e.getMessage shouldEqual "Type should not be Noting"
  }

}
