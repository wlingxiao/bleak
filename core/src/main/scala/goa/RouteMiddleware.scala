package goa

import goa.matcher.PathMatcher

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RouteMiddleware(app: App, pathMatcher: PathMatcher) extends Middleware {

  import RouteMiddleware._

  override def apply(ctx: Context): Future[Response] = {
    Future {
      findMatchedRouter(ctx.request) match {
        case Some(route) =>
          val request = new RequestWithRouterParam(ctx.request, pathMatcher, route)
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

  class RequestWithRouterParam(val request: Request,
                               val pathMatcher: PathMatcher,
                               override val route: Route) extends RequestProxy {

    override def params: Param = {
      val p = pathMatcher.extractUriTemplateVariables(route.path, request.path)
      val splatParam = pathMatcher.extractPathWithinPattern(route.path, request.path)
      if (splatParam != null && !splatParam.isEmpty) {
        p.put("splat", splatParam)
      }
      new RouterParam(request.params, p.toMap)
    }
  }

}