package bleak.swagger3

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.swagger.v3.core.converter.{AnnotatedType, ModelConverterContextImpl}
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media._
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

  def isMap: Boolean = clazz.isAssignableFrom(classOf[Map[_, _]])

  def isArrayOfMap: Boolean = clazz.getSimpleName == "Map[]"

  def isSimpleType: Boolean = isMap || isPrimitive || isArrayOfMap || isString

  def getDefinedModels: Map[String, Schema[_]] = schemaReader.getDefinedModels.asScala.toMap

  def resolve(): Schema[_] = {
    val schema = schemaReader.resolve(new AnnotatedType(clazz).schemaProperty(true))
    schema match {
      case ms: MapSchema =>
        ms.setProperties(null)
      case as: ArraySchema if isArrayOfMap =>
        as.setItems(new MapSchema().additionalProperties(new ObjectSchema()))
      case _ =>
        for ((name, s) <- getDefinedModels) {
          api.getComponents.addSchemas(name, s)
        }
    }
    schema
  }

  def isNothing: Boolean = clazz.isAssignableFrom(classOf[Nothing])

  def isPrimitive: Boolean = clazz.isPrimitive

  def isString: Boolean = clazz.isAssignableFrom(classOf[String])

  def resolveRequestBody(desc: String, mimeTypes: Iterable[String]): RequestBody = {
    val schema = resolve()
    val requestBody = new RequestBody().description(desc)
    val content = new Content
    val mediaType = if (isArray || isSimpleType) {
      new MediaType().schema(schema)
    } else {
      new MediaType().schema(new Schema().$ref(schema.getName))
    }
    mimeTypes.foreach(content.addMediaType(_, mediaType))
    requestBody.setContent(content)
    if (isWwwForm(mimeTypes) || isSimpleType) {
      requestBody
    } else {
      api.getComponents.addRequestBodies(schemaName, requestBody)
      new RequestBody().$ref(schemaName)
    }
  }

  def resolveResponse(desc: String, mimeTypes: Iterable[String]): ApiResponse = {
    val schema = resolve()
    val res = new ApiResponse
    res.setDescription(desc)
    val mediaType = if (isArray || isSimpleType) {
      new MediaType().schema(schema)
    } else {
      new MediaType().schema(new Schema().$ref(schema.getName))
    }
    val content = new Content
    mimeTypes.foreach(content.addMediaType(_, mediaType))
    res.content(content)
  }

  def isWwwForm(mimeTypes: Iterable[String]): Boolean =
    mimeTypes.exists(_.equalsIgnoreCase("application/x-www-form-urlencoded"))

}

object SchemaReader {
  val modelResolver: ModelResolver = {
    Json.mapper().registerModule(DefaultScalaModule)
    new ModelResolver(Json.mapper)
  }
}
