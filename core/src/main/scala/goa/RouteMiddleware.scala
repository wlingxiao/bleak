package goa

import goa.matcher.PathMatcher

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

private[goa] class RouteMiddleware(app: App, pathMatcher: PathMatcher) extends Middleware {

  override def apply(ctx: Context): Future[Response] = {
    Future {
      val r = findMatchedRouter(ctx.request)
      if (r.isDefined) {
        ctx.request.asInstanceOf[Request.Impl].router(r.get)
        runRouterAction(r.get, ctx, pathMatcher)
      } else ctx.notFound()
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

  private def runRouterAction(router: Route, context: Context, pathMatcher: PathMatcher): Response = {
    val requestWithRouter = new RequestWithRouterParam(context.request, pathMatcher)
    context.request(requestWithRouter)
    router.action.apply(context)
  }
}
