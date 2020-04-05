package bleak.swagger3

import io.swagger.v3.oas.models.{OpenAPI, Operation}
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.{ApiResponse, ApiResponses}

import scala.reflect.ClassTag

class RequestBodyBuilder(val api: OpenAPI, val op: Operation)
    extends RequestBodyFactory
    with ResponseFactory
