package goa

import goa.matcher.PathMatcher
import goa.util.Executions

import scala.concurrent.{ExecutionContext, Future}

class RouteMiddleware(app: App, pathMatcher: PathMatcher) extends Middleware {

  import RouteMiddleware._

  protected implicit val ec: ExecutionContext = Executions.directec

  override def apply(ctx: Context): Future[Response] = {
    Future {
      findMatchedRouter(ctx.request) match {
        case Some(route) =>
          val request = new RequestWithPathParam(ctx.request, pathMatcher, route)
          ctx.request(request)
          route.action.apply(ctx)
        case None => ctx.notFound()
      }
    }
  }

  private def findMatchedRouter(request: Request): Option[Route] = {
    val urlMatched = app.routers.filter(r => pathMatcher.tryMatch(r.path, request.path))
    if (urlMatched.isEmpty) {
      return None
    }
    val methodMatched = urlMatched.filter(r => r.methods.contains(request.method))
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