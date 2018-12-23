package bleak.swagger3

import io.swagger.v3.oas.models._

case class PathItemBuilder(method: String, path: String) {

  private var operationBuilder: OperationBuilder = _

  def operation(summary: String,
                desc: String = "",
                tags: Iterable[String] = Nil,
                id: String = "",
                deprecated: Boolean = false): OperationBuilder = {
    operationBuilder = OperationBuilder(summary, desc, tags, buildOperationId(id), deprecated)
    operationBuilder
  }

  def build(openAPI: OpenAPI): PathItem = {
    val paths = openAPI.getPaths
    val pathItem = paths.getOrDefault(path, new PathItem)
    val op = operationBuilder.build(openAPI)
    method match {
      case "get" =>
        pathItem.setGet(op)
      case "post" =>
        pathItem.setPost(op)
      case "put" =>
        pathItem.setPut(op)
      case "delete" =>
        pathItem.setDelete(op)
      case "options" =>
        pathItem.setOptions(op)
      case "head" =>
        pathItem.setHead(op)
      case "patch" =>
        pathItem.setPatch(op)
      case "trace" =>
        pathItem.setTrace(op)
      case _ =>
    }
    pathItem
  }

  private def buildOperationId(id: String): String = {
    if (id.isEmpty) {
      method + path
    } else id
  }

}
