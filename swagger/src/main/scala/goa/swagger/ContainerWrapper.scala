package goa.swagger

import io.swagger.models.properties.{ArrayProperty, MapProperty, Property}

private[swagger] sealed abstract class ContainerWrapper(val container: String) {
  protected def doWrap(property: Property): Property

  def wrap(container: String, property: Property): Property = {
    if (this.container.equalsIgnoreCase(container)) return doWrap(property)
    null
  }
}

private[swagger] object ContainerWrapper {

  def wrapContainer(container: String, property: Property, allowed: ContainerWrapper*): Property = {
    val tmp = if (allowed.nonEmpty) {
      allowed.toSet
    } else Set(LIST, ARRAY, MAP, SET)
    for (wrapper <- tmp) {
      val prop = wrapper.wrap(container, property)
      if (prop != null) {
        return prop
      }
    }
    property
  }
}

private[swagger] object LIST extends ContainerWrapper("list") {
  override protected def doWrap(property: Property): Property = {
    new ArrayProperty(property)
  }
}

private[swagger] object ARRAY extends ContainerWrapper("array") {
  override protected def doWrap(property: Property): Property = new ArrayProperty(property)
}

private[swagger] object MAP extends ContainerWrapper("map") {
  override protected def doWrap(property: Property): Property = new MapProperty(property)
}

private[swagger] object SET extends ContainerWrapper("set") {
  override protected def doWrap(property: Property): Property = {
    val arrayProperty = new ArrayProperty(property)
    arrayProperty.setUniqueItems(true)
    arrayProperty
  }
}
