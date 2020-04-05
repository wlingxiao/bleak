package bleak.swagger3

import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.{Components, OpenAPI, Operation, PathItem, Paths}

class Api {

  def doc(method: String, path: String, summary: String = ""): OperationBuilder = {
    val openAPI = new OpenAPI
    val paths = new Paths
    openAPI.setPaths(paths)
    val op = new Operation
    op.setSummary(summary)
    op.setResponses(new ApiResponses)
    val pathItem = new PathItem
    pathItem.setGet(op)
    paths.addPathItem(path, pathItem)
    openAPI.setComponents(new Components)
    new OperationBuilder(openAPI, op)
  }

}
