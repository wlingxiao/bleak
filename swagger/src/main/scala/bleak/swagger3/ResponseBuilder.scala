package bleak.swagger3

import io.swagger.v3.oas.models.{Components, OpenAPI, Operation, PathItem}
import io.swagger.v3.oas.models.media.{ArraySchema, Content, MediaType, Schema}
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}

class ResponseBuilder(val api: OpenAPI, val op: Operation) extends ResponseFactory {
  def build(): OpenAPI = api
}
