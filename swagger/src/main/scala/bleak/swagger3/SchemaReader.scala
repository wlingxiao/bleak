package bleak.swagger3

import java.lang.reflect.{ParameterizedType, Type}

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.swagger.v3.core.converter.{AnnotatedType, ModelConverterContextImpl}
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.{Content, MediaType, Schema}
import io.swagger.v3.oas.models.parameters.RequestBody
import io.swagger.v3.oas.models.responses.ApiResponse

import scala.jdk.CollectionConverters._
import scala.reflect.{ClassTag, classTag}

class SchemaReader[T: ClassTag](api: OpenAPI) {

  import SchemaReader._

  val clazz: Class[_] = classTag[T].runtimeClass

  private val schemaReader = new ModelConverterContextImpl(modelResolver)

  def schemaName: String = {
    val simpleName = clazz.getSimpleName
    if (isArray) {
      val pos = simpleName.indexOf("[")
      simpleName.substring(0, pos)
    } else simpleName
  }

  def isArray: Boolean = clazz.isArray

  def getDefinedModels: Map[String, Schema[_]] = schemaReader.getDefinedModels.asScala.toMap

  def resolve(): Schema[_] = {
    val schema = schemaReader.resolve(new AnnotatedType(clazz).schemaProperty(true))
    for ((name, s) <- getDefinedModels) {
      api.getComponents.addSchemas(name, s)
    }
    schema
  }

  def isNothing: Boolean = clazz.isAssignableFrom(classOf[Nothing])

  def isPrimitive: Boolean =
    clazz.isAssignableFrom(classOf[Int]) || clazz.isAssignableFrom(classOf[String])

  def resolveRequestBody(desc: String, mediaTypes: Iterable[String]): RequestBody = {
    val schema = resolve()
    val requestBody = new RequestBody().description(desc)
    val content = new Content
    val mediaType = if (isArray) {
      new MediaType()
        .schema(schema)
    } else {
      new MediaType()
        .schema(new Schema().$ref(schema.getName))
    }
    mediaTypes.foreach(content.addMediaType(_, mediaType))
    requestBody.setContent(content)
    if (nonWwwForm(mediaTypes)) {
      api.getComponents.addRequestBodies(schemaName, requestBody)
    }
    requestBody
  }

  def resolveResponse(desc: String, mediaTypes: Iterable[String]): ApiResponse = {
    val schema = resolve()
    val res = new ApiResponse
    res.setDescription(desc)
    val mediaType = new MediaType()
      .schema(new Schema().$ref(schema.getName))
    val content = new Content
    mediaTypes.foreach(content.addMediaType(_, mediaType))
    res.content(content)
  }

  // TODO
  def resolveParameterizedType(obj: Object): Schema[_] = {
    val tpe =
      obj.getClass.getGenericSuperclass
        .asInstanceOf[ParameterizedType]
        .getActualTypeArguments
    schemaReader.resolve(new AnnotatedType(tpe(0)).schemaProperty(true))
  }

  def nonWwwForm(mimeTypes: Iterable[String]): Boolean =
    !mimeTypes.exists(_.equalsIgnoreCase("application/x-www-form-urlencoded"))

}

object SchemaReader {
  val modelResolver: ModelResolver = {
    Json.mapper().registerModule(DefaultScalaModule)
    new ModelResolver(Json.mapper)
  }
}
