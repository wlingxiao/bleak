package bleak.swagger3

import io.swagger.v3.oas.models.Components
import io.swagger.v3.core.converter.ModelConverters
import io.swagger.v3.oas.models.media._
import io.swagger.v3.oas.models.parameters.Parameter
import scala.collection.JavaConverters._
import scala.reflect._

sealed trait ParamIn {
  def name: String
}

object Default extends ParamIn {
  def name: String = ""
}

object Header extends ParamIn {
  def name: String = "header"
}

object Query extends ParamIn {
  def name: String = "query"
}

object Path extends ParamIn {
  def name: String = "path"
}

object Cookie extends ParamIn {
  def name: String = "cookie"
}

case class Param[T: ClassTag](name: String = "",
                              in: ParamIn = Default,
                              desc: String = null) extends SchemaReader[T] {

  def build(components: Components): Iterable[Parameter] = {
    val schema = buildSchema(components)
    schema map { s =>
      val parameter = new Parameter
      parameter.setName(name)
      parameter.setIn(in.name)
      parameter.setSchema(s)
      parameter.setDescription(desc)
      parameter
    }
  }

  def buildSchema(components: Components): Iterable[Schema[_]] = {
    if (isPrimitive) {
      Seq(readMap())
    } else if (isArray) {
      Seq(readArray())
    } else if (isNothing) {
      throw new IllegalArgumentException("Type should not be Noting")
    } else {
      val s = readAll()
      for ((k, v) <- s) {
        components.addSchemas(k, v)
      }
      s(schemaName).getProperties.values().asScala
    }
  }

}
