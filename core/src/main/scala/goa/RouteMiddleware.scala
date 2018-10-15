package goa

import goa.Route.{Charset, Produce}
import goa.matcher.PathMatcher
import goa.util.Executions

import scala.concurrent.{ExecutionContext, Future}

class RouteMiddleware(app: App, pathMatcher: PathMatcher) extends Middleware {

  import RouteMiddleware._

  override def apply(ctx: Context): Future[Response] = {
    findRoute(ctx.request) match {
      case Some(route) =>
        val request = new RequestWithPathParam(ctx.request, pathMatcher, route)
        ctx.request(request)
        ctx.next()
      case None => Future.successful(Ok())
    }
  }

  private def findRoute(request: Request): Option[Route] = {
    val urlMatched = app.routes.filter(r => pathMatcher.tryMatch(r.path, request.path))
    if (urlMatched.isEmpty) {
      return None
    }
    val methodMatched = urlMatched.filter(r => r.method == request.method)
    if (methodMatched.isEmpty) {
      return None
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    })
    finalMatched.headOption
  }
}

object RouteMiddleware {

  import util.RicherString._

  class RequestWithPathParam(val request: Request,
                             val pathMatcher: PathMatcher,
                             override val route: Route) extends RequestProxy {

    override def params: Param = {
      val pathParam = pathMatcher.extractUriTemplateVariables(route.path, request.path)
      val pattern = pathMatcher.extractPathWithinPattern(route.path, request.path)
      val splat = if (pattern.nonBlank) Some(pattern) else None
      new PathParam(request.params, pathParam.toMap, splat)
    }
  }

  class PathParam(paramMap: Param,
                  params: Map[String, String],
                  override val splat: Option[String]) extends Param {

    def get(key: String): Option[String] = {
      params.get(key) orElse paramMap.get(key)
    }

    override def getAll(key: String): Iterable[String] = {
      params.get(key) ++ paramMap.getAll(key)
    }
  }

}

class ActionExecutionMiddleware extends Middleware {

  protected implicit val ec: ExecutionContext = Executions.directec

  override def apply(ctx: Context): Future[Response] = {
    val route = ctx.request.route
    Future {
      val ret = route.action(ctx)
      ret
    }
  }

  private def convertResultToResponse(ret: Result, route: Route): Response = {
    val headers = Headers(ret.headers.toSeq: _*)
    route.attr[Produce].foreach { produce =>
      val charset = route.attr[Charset].map(";" + _.value).getOrElse("")
      val contentType = produce.value.headOption.getOrElse("")
      headers.add(Fields.ContentType, contentType + charset)
    }
    val cookies = Cookies(ret.cookies.toSet)
    Response(status = ret.status, headers = headers, cookies = cookies, body = ret.body)
  }

}