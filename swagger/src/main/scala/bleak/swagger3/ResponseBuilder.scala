package bleak.swagger3

import io.swagger.v3.oas.models.{Components, OpenAPI, Operation, PathItem}
import io.swagger.v3.oas.models.media.{ArraySchema, Content, MediaType, Schema}
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}

class ResponseBuilder(api: OpenAPI, op: Operation) {

  def response(name: String = "200", desc: String, mimeType: String): ResponseBuilder = {
    val res = new ApiResponse
    res.setDescription(desc)
    val mediaType = new MediaType
    // mediaType.setSchema()
    val content = new Content
    content.addMediaType(mimeType, mediaType)
    op.getResponses.addApiResponse(name, res)
    this
  }

  def build(): OpenAPI = api

}
