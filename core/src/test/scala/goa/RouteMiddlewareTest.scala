package goa

import goa.annotation.{AnnotationProcessor, GET, Path}
import org.mockito.Mockito

private case class RouteMiddlewareParam(id: Long, name: String)

@Path("/route")
private class RouteMiddlewareController {

  @GET def listAll(param: RouteMiddlewareParam): Unit = {}

}

@Path("/primitive")
private class PrimitiveTypeController {
  @GET def listAll(id: Long, name: String): Unit = {}
}

class RouteMiddlewareTest extends BaseTests {

  val routeMiddleware = new RouteMiddleware(null, null, null)

  val processor = new AnnotationProcessor

  test("map query param to case class") {
    val request = Mockito.mock(classOf[Request])
    val param = Mockito.mock(classOf[Param])
    val map = Map("id" -> "123456", "name" -> "admin")
    Mockito.when(param.get("id")).thenReturn(map.get("id"))
    Mockito.when(param.get("name")).thenReturn(map.get("name"))
    Mockito.when(request.params).thenReturn(param)
    Mockito.when(param.flat()).thenReturn(map)
    val routes = processor.process(new RouteMiddlewareController)

    val ret = routeMiddleware.mapRouteParam(routes.head, request)

    ret.asInstanceOf[Map[String, RouteMiddlewareParam]]("param") shouldEqual RouteMiddlewareParam(123456, "admin")
  }

  test("map query param to primitive type") {
    val request = Mockito.mock(classOf[Request])
    val param = Mockito.mock(classOf[Param])
    val map = Map("id" -> "123456", "name" -> "admin")
    Mockito.when(param.get("id")).thenReturn(map.get("id"))
    Mockito.when(param.get("name")).thenReturn(map.get("name"))
    Mockito.when(request.params).thenReturn(param)
    Mockito.when(param.flat()).thenReturn(map)
    val route = processor.process(new PrimitiveTypeController).head

    val ret = routeMiddleware.mapRouteParam(route, request)

    ret("id") shouldEqual 123456L
    ret("name") shouldEqual "admin"
  }

}
