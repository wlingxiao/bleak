package bleak.swagger3

import io.swagger.v3.oas.models.{OpenAPI, Operation}
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}

class RequestBodyBuilder(api: OpenAPI, op: Operation) {

  def response(name: String, desc: String, mimeType: String): ResponseBuilder = {
    val res = new ApiResponse
    res.setDescription(desc)
    val mediaType = new MediaType
    // mediaType.setSchema()
    val content = new Content
    content.addMediaType(mimeType, mediaType)
    val apiResponse = new ApiResponses
    apiResponse.addApiResponse(name, res)
    op.setResponses(apiResponse)
    new ResponseBuilder(api, op)
  }

  def requestBody(desc: String, mimeType: Iterable[String]): RequestBodyBuilder = {
    val requestBody = new RequestBody
    val content = new Content
    mimeType.map { mt =>
      val m = new MediaType
      // m.setSchema()
      content.addMediaType(mt, m)
    }
    requestBody.setContent(content)
    op.setRequestBody(requestBody)
    new RequestBodyBuilder(api, op)
  }

}
