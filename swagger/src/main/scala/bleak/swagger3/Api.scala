package bleak.swagger3

import bleak.Route
import io.swagger.v3.oas.models.PathItem.HttpMethod
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.servers.Server
import io.swagger.v3.oas.models.tags.Tag
import io.swagger.v3.oas.models.{Components, OpenAPI, Operation, PathItem, Paths}

import scala.jdk.CollectionConverters._

trait Api {

  val openAPI: OpenAPI = new OpenAPI()
    .paths(new Paths)
    .components(new Components)

  def doc(
      method: String,
      path: String,
      summary: String = "",
      tags: Iterable[String] = Nil): OperationBuilder = {
    val op = new Operation
    op.setSummary(summary)
    op.setResponses(new ApiResponses)
    op.setTags(tags.toList.asJava)
    val pathItem = new PathItem
    pathItem.operation(HttpMethod.valueOf(method.toUpperCase()), op)
    openAPI.getPaths.addPathItem(path, pathItem)
    new OperationBuilder(openAPI, op)
  }

  def apply(route: Route, summary: String = ""): OperationBuilder =
    doc(route.method.name(), route.path, summary)

  def config(info: Info, tags: Iterable[Tag], servers: Iterable[Server]): Unit =
    openAPI
      .info(info)
      .tags(tags.toList.asJava)
      .servers(servers.toList.asJava)
}

object Api extends Api
