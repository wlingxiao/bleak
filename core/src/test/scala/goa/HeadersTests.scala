package goa

class HeadersTests extends BaseTests {

  test("test Headers operation") {
    val headerMap = Headers()
    headerMap.add("Content-Type", "text/html")
    headerMap.add("Content-Type", "application/json")

    headerMap.get("Content-Type") shouldEqual Some("text/html")
    headerMap.getOrNull("Content-Length") shouldEqual null

    headerMap.getAll("Content-Type") shouldEqual Seq("text/html", "application/json")
    headerMap.getAll("Content-Length") shouldEqual Seq.empty

    headerMap.set("Content-Type", "text/javascript")
    headerMap.get("Content-Type") shouldEqual Some("text/javascript")
    headerMap.getAll("Content-Type") shouldEqual Seq("text/javascript")

  }

}
