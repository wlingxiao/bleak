package bleak.swagger3

import io.swagger.v3.oas.models.media.{ArraySchema, MapSchema}
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.{Components, OpenAPI, Operation}
import org.specs2.mutable.Specification

class ResponseBuilderSpec extends Specification {

  def newOpenApi(): OpenAPI = new OpenAPI().components(new Components)

  "ResponseBuilder.response" should {
    "resolve response for case class" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      operation.setResponses(new ApiResponses)
      val responseBuilder = new ResponseBuilder(openAPI, operation)

      responseBuilder.response[User]("200", "Success", Seq("application/json"))
      openAPI.getComponents.getSchemas.get("User").getName should_== "User"
      operation.getResponses
        .get("200")
        .getContent
        .get("application/json")
        .getSchema
        .get$ref() should_== "#/components/schemas/User"
    }

    "resolve response for array" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      operation.setResponses(new ApiResponses)
      val responseBuilder = new ResponseBuilder(openAPI, operation)

      responseBuilder.response[Array[User]]("200", "Success", Seq("application/json"))
      openAPI.getComponents.getSchemas.get("User").getName should_== "User"
      operation.getResponses
        .get("200")
        .getContent
        .get("application/json")
        .getSchema
        .asInstanceOf[ArraySchema]
        .getItems
        .get$ref() should_== "#/components/schemas/User"
    }

    "resolve response for map" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      operation.setResponses(new ApiResponses)
      val responseBuilder = new ResponseBuilder(openAPI, operation)

      responseBuilder.response[Map[_, _]]("200", "Success", Seq("application/json"))
      openAPI.getComponents.getSchemas should_== null
      operation.getResponses
        .get("200")
        .getContent
        .get("application/json")
        .getSchema
        .getType should_== "object"
    }

    "resolve response for array of map" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      operation.setResponses(new ApiResponses)
      val responseBuilder = new ResponseBuilder(openAPI, operation)

      responseBuilder.response[Array[Map[_, _]]]("200", "Success", Seq("application/json"))

      openAPI.getComponents.getSchemas should_== null
      operation.getResponses
        .get("200")
        .getContent
        .get("application/json")
        .getSchema
        .asInstanceOf[ArraySchema]
        .getItems
        .getType should_== "object"
    }

    "resolve nothing for response" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      operation.setResponses(new ApiResponses)
      val responseBuilder = new ResponseBuilder(openAPI, operation)

      responseBuilder.response("200", "Success")

      openAPI.getComponents.getSchemas should_== null
      operation.getResponses
        .get("200")
        .getDescription should_== "Success"
    }
  }

}
