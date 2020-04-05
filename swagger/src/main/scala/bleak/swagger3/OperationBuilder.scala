package bleak.swagger3

import io.swagger.v3.oas.models.{OpenAPI, Operation}

class OperationBuilder(val api: OpenAPI, val op: Operation)
    extends RequestBodyFactory
    with ResponseFactory
    with ParamFactory
