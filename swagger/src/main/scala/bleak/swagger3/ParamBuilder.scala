package bleak.swagger3

import io.swagger.v3.oas.models.{OpenAPI, Operation}

class ParamBuilder(val api: OpenAPI, val op: Operation)
    extends ParamFactory
    with RequestBodyFactory
    with ResponseFactory
