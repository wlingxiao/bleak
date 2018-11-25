package bleak.util

import bleak.BaseTests

class AttributeTests extends BaseTests {

  private val attrMap = new AttributeMap() {}

  test("test set value into Attribute and get value from Attribute") {
    val key = "key"
    attrMap.attr[String](key).get shouldEqual None
    attrMap.attr[String](key).set("value")
    attrMap.attr[String](key).get shouldEqual Some("value")
  }

}
