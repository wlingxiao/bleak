package goa.util

import goa.BaseTests

class QueryStringDecoderTests extends BaseTests {

  test("test decode") {
    val uri = "http://localhost/test?name=user&age=10"
    val params = QueryStringDecoder.decode(uri)
    params("name")(0) shouldEqual "user"
    params("age")(0) shouldEqual "10"

    val multiParamUri = "http://localhost/test?person=one&person=two&sex=male"
    val multiParam = QueryStringDecoder.decode(multiParamUri)
    multiParam("person")(0) shouldEqual "one"
    multiParam("person")(1) shouldEqual "two"
    multiParam("sex")(0) shouldEqual "male"
  }

}
