package bleak
package swagger3

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media._
import io.swagger.v3.oas.models.parameters.RequestBody

import scala.reflect.ClassTag

class RequestBodyBuilder[T: ClassTag](desc: String, mimeType: String) extends SchemaReader[T] {

  def build(components: Components, requestBody: RequestBody): Unit = {
    if (isNothing) {
      return
    }
    if (isWwwForm || isMap || isArray) {
      val s = buildSchema(components)
      requestBody.setDescription(desc)
      requestBody.setContent(buildContent(s))
    } else {
      buildSchema(components)
      requestBody.set$ref(schemaName)
    }
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

  private def isWwwForm: Boolean = {
    mimeType == MimeType.WwwForm
  }

  private def buildContent(s: Schema[_]): Content = {
    val content = new Content
    val mediaType = new MediaType
    val ss = if (s == null) {
      val ss = new Schema[Int]
      ss.$ref(schemaName)
    } else s
    mediaType.setSchema(ss)
    content.addMediaType(mimeType, mediaType)
  }

}
