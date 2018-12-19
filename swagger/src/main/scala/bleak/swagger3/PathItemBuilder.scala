package bleak.swagger3

import io.swagger.v3.oas.models._

case class PathItemBuilder(method: String, path: String) {

  private var operationBuilder: OperationBuilder = _

  def operation(summary: String,
                desc: String = "",
                tags: Iterable[String] = Nil,
                id: String = ""): OperationBuilder = {
    operationBuilder = OperationBuilder(summary, desc, tags, id)
    operationBuilder
  }

  def build(openAPI: OpenAPI): PathItem = {
    val paths = openAPI.getPaths
    val pathItem = paths.getOrDefault(path, new PathItem)
    method match {
      case "get" =>
        val op = operationBuilder.build(openAPI)
        op.setOperationId(operationId())
        pathItem.setGet(op)
      case "post" =>
        val op = operationBuilder.build(openAPI)
        op.setOperationId(operationId())
        pathItem.setPost(op)
      case _ =>
    }
    pathItem
  }

  private def operationId(): String = {
    if (operationBuilder.id.isEmpty) {
      method + path
    } else operationBuilder.id
  }

}
