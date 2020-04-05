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
    val sr = new SchemaReader[T](api)
    val requestBody = sr.resolveRequestBody(desc, mimeTypes)
    val rb = if (nonWwwForm(mimeTypes)) {
      new RequestBody().$ref(sr.schemaName)
    } else requestBody
    requestBody.setRequired(required)
    op.setRequestBody(rb)
    new RequestBodyBuilder(api, op)
  }

  def nonWwwForm(mimeTypes: Iterable[String]): Boolean =
    !mimeTypes.exists(_.equalsIgnoreCase("application/x-www-form-urlencoded"))

}
