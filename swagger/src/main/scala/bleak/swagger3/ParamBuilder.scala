package bleak.swagger3

import io.swagger.v3.oas.models.{OpenAPI, Operation}
import io.swagger.v3.oas.models.media.{Content, MediaType}
import io.swagger.v3.oas.models.parameters.{Parameter, RequestBody}

class ParamBuilder(val api: OpenAPI, val op: Operation) extends ParamFactory with RequestBodyFactory
