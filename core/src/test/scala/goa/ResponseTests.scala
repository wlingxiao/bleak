package goa

class ResponseTests extends BaseTests {

  test("put cookie into response") {
    val response = Response()
    response.cookies.add(Cookie("name", "111111"))
    response.cookies.get("name") shouldEqual Some(Cookie("name", "111111"))

    response.headers.get(Fields.SetCookie).get should include("name=111111")
  }

}
