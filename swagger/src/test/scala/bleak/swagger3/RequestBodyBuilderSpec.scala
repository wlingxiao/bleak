package bleak.swagger3

import io.swagger.v3.oas.models.media.ArraySchema
import io.swagger.v3.oas.models.{Components, OpenAPI, Operation}
import org.specs2.mutable.Specification

class RequestBodyBuilderSpec extends Specification {

  def newOpenApi(): OpenAPI = new OpenAPI().components(new Components)

  "RequestBodyBuilder.requestBody" should {
    "resolve request body for case class" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      val requestBodyChain = new RequestBodyBuilder(openAPI, operation)

      requestBodyChain.requestBody[User]("user", Seq("application/json"))

      openAPI.getComponents.getSchemas.get("User").getName should_== "User"
      openAPI.getComponents.getRequestBodies
        .get("User")
        .getContent
        .get("application/json")
        .getSchema
        .get$ref() should_== "#/components/schemas/User"
      operation.getRequestBody.get$ref() should_== "#/components/requestBodies/User"
    }

    "resolve request body for array" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      val requestBodyChain = new RequestBodyBuilder(openAPI, operation)

      requestBodyChain.requestBody[Array[User]]("user", Seq("application/json"))

      openAPI.getComponents.getSchemas.get("User").getName should_== "User"
      operation.getRequestBody.get$ref() should_== "#/components/requestBodies/User"
      openAPI.getComponents.getRequestBodies
        .get("User")
        .getContent
        .get("application/json")
        .getSchema
        .asInstanceOf[ArraySchema]
        .getItems
        .get$ref() should_== "#/components/schemas/User"
    }

    "resolve request body for application/x-www-form-urlencoded" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      val requestBodyChain = new RequestBodyBuilder(openAPI, operation)

      requestBodyChain.requestBody[User]("user", Seq("application/x-www-form-urlencoded"))

      operation.getRequestBody.getContent
        .get("application/x-www-form-urlencoded")
        .getSchema
        .get$ref() should_== "#/components/schemas/User"
      openAPI.getComponents.getSchemas.get("User").getName should_== "User"
      openAPI.getComponents.getRequestBodies should_== null
    }

    "resolve request body for scala map" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      val requestBodyChain = new RequestBodyBuilder(openAPI, operation)

      requestBodyChain.requestBody[Map[_, _]]("user", Seq("application/application/json"))

      val schema = operation.getRequestBody.getContent
        .get("application/application/json")
        .getSchema
      schema.getType should_== "object"
      openAPI.getComponents.getSchemas should_== null
      openAPI.getComponents.getRequestBodies should_== null
    }

    "resolve request body for array of map" in {
      val openAPI = newOpenApi()
      val operation = new Operation
      val requestBodyChain = new RequestBodyBuilder(openAPI, operation)

      requestBodyChain.requestBody[Array[Map[_, _]]]("user", Seq("application/application/json"))

      val schema = operation.getRequestBody.getContent
        .get("application/application/json")
        .getSchema
        .asInstanceOf[ArraySchema]
        .getItems
      schema.getType should_== "object"
      openAPI.getComponents.getSchemas should_== null
      openAPI.getComponents.getRequestBodies should_== null
    }

  }

}
