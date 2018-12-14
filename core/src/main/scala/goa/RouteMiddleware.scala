package goa

import goa.Route.{Charset, Produce}
import goa.matcher.PathMatcher
import goa.util.Executions

import scala.concurrent.{ExecutionContext, Future}

class RouteMiddleware(app: App) extends Middleware {

  import RouteMiddleware._

  override def apply(ctx: Context): Future[Context] = {
    findRoute(app, ctx)
  }

  private def findRoute(app: App, ctx: Context): Future[Context] = {
    val request = ctx.request
    val pathMatcher = app.pathMatcher
    val urlMatched = app.routes.filter(r => pathMatcher.tryMatch(r.path, request.path))
    if (urlMatched.isEmpty) {
      return Future.successful(ctx.status(Status.NotFound))
    }
    val methodMatched = urlMatched.filter(r => r.method == request.method)
    if (methodMatched.isEmpty) {
      return Future.successful(ctx.status(Status.MethodNotAllowed))
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    })
    finalMatched.headOption match {
      case Some(route) =>
        ctx.request(new RequestWithPathParam(ctx.request, pathMatcher, route)).next()
      case None => Future.successful(null)
    }
  }
}

object RouteMiddleware {

  import util.RicherString._

  class RequestWithPathParam(val request: Request,
                             val pathMatcher: PathMatcher,
                             override val route: Route) extends RequestProxy {

    override def params: Params = {
      val pathParam = pathMatcher.extractUriTemplateVariables(route.path, request.path)
      val pattern = pathMatcher.extractPathWithinPattern(route.path, request.path)
      val splat = if (pattern.nonBlank) Some(pattern) else None
      new PathParams(request.params, pathParam.toMap, splat)
    }
  }

  class PathParams(paramMap: Params,
                   params: Map[String, String],
                   override val splat: Option[String]) extends Params {

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

  override def apply(ctx: Context): Future[Context] = {
    val route = ctx.request.route
    Future {
      val ret = route.action(ctx)
      val response = ctx.response
      val headers = Headers(ret.headers.toSeq: _*)
      route.attr[Produce].foreach { produce =>
        val charset = route.attr[Charset].map(";" + _.value).getOrElse("")
        val contentType = produce.value.headOption.getOrElse("")
        headers.add(Fields.ContentType, contentType + charset)
      }
      val cookies = Cookies(ret.cookies.toSet)
      ctx.response = response.status(ret.status).headers(headers).cookies(cookies).body(ret.body)
    }
  }

}