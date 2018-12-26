package bleak
package swagger3

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.{MediaType, Schema}

import scala.reflect.ClassTag

case class Consume[T: ClassTag](mimeType: String = "") extends SchemaReader[T] {
  private def isWwwForm: Boolean = {
    mimeType == MimeType.WwwForm
  }

  def build(components: Components): MediaType = {
    if (isWwwForm || isMap || isPrimitive || isArray) {
      val s = buildSchema(components)
      buildContent(s)
    } else {
      buildSchema(components)
      buildContent(null)
    }
  }

  private def buildContent(s: Schema[_]): MediaType = {
    val mediaType = new MediaType
    val ss = if (s == null) {
      val ss = new Schema[Int]
      ss.$ref(schemaName)
    } else s
    mediaType.setSchema(ss)
    mediaType
  }

  private def buildSchema(components: Components): Schema[_] = {
    if (isArray) {
      readArray()
    } else if (isMap) {
      readMap()
    } else {
      val s = readAll()
      for ((k, v) <- s) {
        components.addSchemas(k, v)
      }
      components.getSchemas.get(schemaName)
    }
  }

}
