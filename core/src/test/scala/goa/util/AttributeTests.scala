package goa.util

import goa.BaseTests

class AttributeTests extends BaseTests {

  private val attrMap = AttributeMap()

  test("test set if absent") {
    val key = AttributeKey.create[String]("one-key")

    attrMap.attr(key)

    attrMap.hasAttr(key) shouldBe true

    attrMap.attr(key).get shouldBe null

    attrMap.attr(key).setIfAbsent("one-value")
    attrMap.attr(key).get shouldEqual "one-value"
  }

}
