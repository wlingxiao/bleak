package bleak.swagger3

import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.{OpenAPI, Operation}

import scala.reflect.ClassTag

trait RequestBodyFactory {

  def api: OpenAPI

  def op: Operation

  def requestBody[T: ClassTag](
      desc: String,
      mimeTypes: Iterable[String],
      required: Boolean = true): RequestBodyBuilder = {
    val requestBody = new SchemaReader[T](api).resolveRequestBody(desc, mimeTypes)
    requestBody.setRequired(required)
    op.setRequestBody(requestBody)
    new RequestBodyBuilder(api, op)
  }

}
