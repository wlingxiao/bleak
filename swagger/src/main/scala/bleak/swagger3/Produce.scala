package bleak.swagger3

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.media.{ArraySchema, Content, MediaType, Schema}
import io.swagger.v3.oas.models.responses.ApiResponse

import scala.reflect.ClassTag

case class Produce[T: ClassTag](name: String = "200",
                                desc: String = "Success",
                                mimeType: String = "") extends SchemaReader[T] {

  def build(components: Components): ApiResponse = {
    val res = new ApiResponse
    res.setDescription(desc)
    res.setContent(buildContent(components))
    res
  }

  private def buildSchema(components: Components): Schema[_] = {
    val s = readAll()
    for ((k, v) <- s) {
      components.addSchemas(k, v)
    }
    components.getSchemas.get(schemaName)
  }

  private def buildContent(components: Components): Content = {
    val content = new Content
    if (isNothing) {
      return content
    }
    val mediaType = new MediaType
    if (isArray) {
      val s = new ArraySchema()
      s.items(new Schema[Int].$ref(schemaName))
      mediaType.setSchema(s)
    } else if (isMap || isPrimitive) {
      val s = readMap()
      mediaType.setSchema(s)
    } else {
      buildSchema(components)
      mediaType.setSchema(new Schema[Int].$ref(schemaName))
    }
    content.addMediaType(mimeType, mediaType)
  }

}
