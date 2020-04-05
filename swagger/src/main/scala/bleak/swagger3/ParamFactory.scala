package bleak.swagger3

import io.swagger.v3.oas.models.{OpenAPI, Operation}
import io.swagger.v3.oas.models.parameters.Parameter

import scala.reflect.ClassTag

trait ParamFactory {

  def api: OpenAPI

  def op: Operation

  def path[T: ClassTag](name: String, desc: String = null): ParamBuilder =
    param(name, "path", desc, required = true)

  def query[T: ClassTag](
      name: String,
      desc: String = null,
      required: Boolean = false): ParamBuilder =
    param(name, "query", desc, required)

  def header[T: ClassTag](
      name: String,
      desc: String = null,
      required: Boolean = false): ParamBuilder =
    param(name, "header", desc, required)

  def cookie[T: ClassTag](
      name: String,
      desc: String = null,
      required: Boolean = false): ParamBuilder =
    param(name, "cookie", desc, required)

  def param[T: ClassTag](
      name: String,
      in: String,
      desc: String = null,
      required: Boolean = false): ParamBuilder = {
    val sr = new SchemaReader[T](api)
    val parameter = new Parameter
    parameter.setName(name)
    parameter.setIn(in)
    parameter.setRequired(required)
    parameter.setSchema(sr.resolveParam())
    parameter.setDescription(desc)
    op.addParametersItem(parameter)
    new ParamBuilder(api, op)
  }

}
