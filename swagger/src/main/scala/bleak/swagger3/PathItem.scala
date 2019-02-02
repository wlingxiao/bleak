package bleak
package swagger3

import io.swagger.v3.oas.models.{OpenAPI, PathItem => SPathItem}
import bleak.Method._

/**
  * Builder for [[io.swagger.v3.oas.models.PathItem]]
  */
case class PathItem(method: Method, path: String) {

  private var operationBuilder: OperationBuilder = _

  def operation(summary: String,
                desc: String = "",
                tags: Iterable[String] = Nil,
                id: String = "",
                deprecated: Boolean = false): OperationBuilder = {
    operationBuilder = OperationBuilder(summary, desc, tags, buildOperationId(id), deprecated)
    operationBuilder
  }

  def build(openAPI: OpenAPI): SPathItem = {
    val paths = openAPI.getPaths
    val pathItem = paths.getOrDefault(path, new SPathItem)
    val op = operationBuilder.build(openAPI)
    method match {
      case Get =>
        pathItem.setGet(op)
      case Post =>
        pathItem.setPost(op)
      case Put =>
        pathItem.setPut(op)
      case Delete =>
        pathItem.setDelete(op)
      case Options =>
        pathItem.setOptions(op)
      case Head =>
        pathItem.setHead(op)
      case Patch =>
        pathItem.setPatch(op)
      case Trace =>
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
