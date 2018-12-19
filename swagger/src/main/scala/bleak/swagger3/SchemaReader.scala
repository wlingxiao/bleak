package bleak.swagger3

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import io.swagger.v3.core.converter.AnnotatedType
import io.swagger.v3.core.jackson.ModelResolver
import io.swagger.v3.core.util.Json
import io.swagger.v3.oas.models.media.Schema

import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}

abstract class SchemaReader[T: ClassTag] {

  import SchemaReader._

  private val schemaReader = new ScalaModelConverterContext(modelResolver)

  private val clazz = classTag[T].runtimeClass

  def schemaName: String = {
    val simpleName = clazz.getSimpleName
    if (isArray) {
      val pos = simpleName.indexOf("[")
      simpleName.substring(0, pos)
    } else simpleName
  }

  def isArray: Boolean = {
    clazz.isArray
  }

  def isMap: Boolean = {
    clazz.isAssignableFrom(classOf[Map[_, _]])
  }

  def readAll(): Map[String, Schema[_]] = {
    val tpe = new AnnotatedType(clazz)
    schemaReader.resolve(tpe)
    schemaReader.getDefinedModels.asScala.toMap
  }

  def readArray(): Schema[_] = {
    val tpe = new AnnotatedType(clazz).schemaProperty(true)
    schemaReader.resolve(tpe)
  }

  def readMap(): Schema[_] = {
    val tpe = new AnnotatedType(clazz).schemaProperty(true)
    schemaReader.resolve(tpe)
  }

  def isNothing: Boolean = {
    clazz.isAssignableFrom(classOf[Nothing])
  }

  def isPrimitive: Boolean = {
    clazz.isAssignableFrom(classOf[Int]) || clazz.isAssignableFrom(classOf[String])
  }

}

object SchemaReader {
  private val modelResolver = {
    Json.mapper().registerModule(DefaultScalaModule)
    new ModelResolver(Json.mapper)
  }
}