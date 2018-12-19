package bleak
package swagger3

import com.fasterxml.jackson.databind.`type`.{MapLikeType, MapType}
import io.swagger.v3.core.converter.{AnnotatedType, ModelConverter, ModelConverterContextImpl}
import io.swagger.v3.oas.models.media.Schema

class ScalaModelConverterContext(converter: ModelConverter) extends ModelConverterContextImpl(converter) {
  override def resolve(tpe: AnnotatedType): Schema[_] = {
    val javaType = tpe.getType
    javaType match {
      case mapLike: MapLikeType =>
        tpe.setType(MapType.construct(classOf[java.util.Map[_, _]], mapLike.getBindings, null, Array.empty, mapLike.getKeyType, mapLike.getContentType))
      case map: Class[_] if map.isAssignableFrom(classOf[Map[_, _]]) =>
        tpe.setType(classOf[java.util.Map[_, _]])
      case _ =>
    }
    super.resolve(tpe)
  }
}
