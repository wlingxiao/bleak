package goa

class ResponseTests extends BaseTests {

  test("put cookie into response") {
    val response = Response()
    response.cookies.add(Cookie("name", "111111"))
    response.cookies("name").name shouldEqual "name"
    response.cookies("name").value shouldEqual Some("111111")
    response.headers(Fields.SetCookie) should include("name=111111")
  }

}
