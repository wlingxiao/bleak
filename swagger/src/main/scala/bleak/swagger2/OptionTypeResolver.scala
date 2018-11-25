package bleak.swagger2

import java.lang.reflect.Type

import io.swagger.util.Json

private[swagger2] object OptionTypeResolver {

  private val optionTypeInt: Option[Integer] = null
  private val optionTypeLong: Option[Long] = null
  private val optionTypeByte: Option[Byte] = null
  private val optionTypeBoolean: Option[Boolean] = null
  private val optionTypeChar: Option[Char] = null
  private val optionTypeFloat: Option[Float] = null
  private val optionTypeDouble: Option[Double] = null
  private val optionTypeShort: Option[Short] = null

  def resolveOptionType(innerType: String, cls: Class[_]): Type = {
    try {
      Json.mapper().getTypeFactory.constructType(getClass.getDeclaredField("getDeclaredField" + innerType).getGenericType, cls)
    } catch {
      case e: NoSuchFieldException =>
        null
    }
  }

}
