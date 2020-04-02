package bleak

import bleak.Params.{CombinedParams, PathParams, QueryParams}
import bleak.matcher.AntPathMatcher

class CombinedParamsSpec extends Spec {

  "CombinedParams.getAll" should {
    "returns all element from params" in {
      val queryParams = new QueryParams("http://localhost/hello?foo=bar")
      val pathParams = new PathParams("/{foo}", "/bar1", new AntPathMatcher)

      val combined = new CombinedParams(queryParams, pathParams)
      combined.getAll("foo") should_== Seq("bar", "bar1")

    }
  }

  "CombinedParams.splat" should {
    "returns splat param" in {
      val pathParams = new PathParams("/**", "/bar1", new AntPathMatcher)
      val pathParams1 = new PathParams("/{foo}", "/bar", new AntPathMatcher)

      val combined = new CombinedParams(pathParams1, pathParams)
      combined.splat should_=== Some("bar1")
    }
  }

}
