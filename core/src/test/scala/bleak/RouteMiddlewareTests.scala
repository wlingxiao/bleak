package bleak

import bleak.RouteMiddleware.RequestWithPathParam
import bleak.matcher.AntPathMatcher
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class RouteMiddlewareTests extends BaseTests with MockitoSugar {

  test("RequestWithPathParam.params should return parameter in route path") {
    val route = mock[Route]
    when(route.path).thenReturn("/test/{id}")
    val request = mock[Request]
    when(request.path).thenReturn("/test/123")
    val pathMatcher = new AntPathMatcher

    val requestWithPathParam = new RequestWithPathParam(request, pathMatcher, route)
    requestWithPathParam.params.get("id") shouldEqual Some("123")
    requestWithPathParam.params.splat shouldEqual None
  }

  test("RequestWithPathParam.splat should return splat string in route path") {
    val route = mock[Route]
    when(route.path).thenReturn("/test/*/hello/*")
    val request = mock[Request]
    when(request.path).thenReturn("/test/123/hello/456")
    val pathMatcher = new AntPathMatcher

    val requestWithPathParam = new RequestWithPathParam(request, pathMatcher, route)
    requestWithPathParam.params.splat shouldEqual Some("123/hello/456")
  }

}
