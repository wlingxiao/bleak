package bleak
package swagger3

import io.swagger.v3.oas.models.media.{ArraySchema, StringSchema}
import io.swagger.v3.oas.models.{Components, OpenAPI, Operation}

class OperationBuilderSpec extends Spec {

  def newOpenApi(): OpenAPI = new OpenAPI().components(new Components)

  "OperationBuilder.param" should {
    "build param for case class" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      val operationBuilder = new OperationBuilder(openAPI, operation)

      operationBuilder.param[User]("id", "header")

      openAPI.getComponents.getSchemas.get("User") should_!= null
      val param = operation.getParameters.get(0)
      param.getName should_== "id"
      param.getIn should_== "header"
      param.getSchema.get$ref() should_== "#/components/schemas/User"
    }

    "build param for array" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      val operationBuilder = new OperationBuilder(openAPI, operation)

      operationBuilder.param[Array[String]]("id", "header")

      openAPI.getComponents.getSchemas should_== null
      val param = operation.getParameters.get(0)
      param.getName should_== "id"
      param.getIn should_== "header"
      param.getSchema
        .asInstanceOf[ArraySchema]
        .getItems
        .isInstanceOf[StringSchema] should_== true
    }
  }

}
