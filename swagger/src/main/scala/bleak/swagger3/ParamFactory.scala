package bleak.swagger3

import io.swagger.v3.oas.models.{OpenAPI, Operation}
import io.swagger.v3.oas.models.parameters.Parameter

trait ParamFactory {

  def api: OpenAPI

  def op: Operation

  def query(name: String, desc: Option[String]): ParamBuilder = {
    val parameter = new Parameter
    parameter.setName(name)
    parameter.setIn("query")
    // parameter.setSchema(s)
    parameter.setDescription(desc.orNull)
    op.addParametersItem(parameter)
    new ParamBuilder(api, op)
  }

}
