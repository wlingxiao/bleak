package bleak

import bleak.Params.QueryParams

class QueryParamsSpec extends Spec {

  "QueryParams.get" should {
    "get a element from params" in {
      val uri = "http://localhost/foo?hello=world"
      val queryParams = new QueryParams(uri)

      queryParams.get("hello") should_=== Some("world")
    }
  }

  "QueryParams.getAll" should {
    "get a list of element from params" in {
      val uri = "http://localhost/foo?hello=world&hello=bar"
      val queryParams = new QueryParams(uri)

      queryParams.getAll("hello") should_== Iterable("world", "bar")
    }
  }

}
