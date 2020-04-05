package bleak.swagger3

import io.swagger.v3.oas.models.{OpenAPI, Operation}

import scala.reflect.ClassTag

trait ResponseFactory {

  def api: OpenAPI

  def op: Operation

  def response[T: ClassTag](
      name: String,
      desc: String,
      mimeTypes: Iterable[String] = Nil): ResponseBuilder = {
    val res = new SchemaReader[T](api).resolveResponse(desc, mimeTypes)
    op.getResponses.addApiResponse(name, res)
    new ResponseBuilder(api, op)
  }

}
