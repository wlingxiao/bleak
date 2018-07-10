package goa

import goa.marshalling.MessageBodyWriter
import goa.matcher.PathMatcher

private class RouteMiddleware(mapper: MessageBodyWriter, app: Application, pathMatcher: PathMatcher) extends Middleware {
  override def apply(ctx: Context): Unit = {
    val r = findMatchedRouter(request)
    if (r != null) {
      runRouterAction(r.iterator, request, response, pathMatcher)
    }
  }

  private def findMatchedRouter(request: Request): Seq[Route] = {
    val urlMatched = app.routers.filter(r => pathMatcher.tryMatch(r.path, request.path))
    if (urlMatched.isEmpty) {
      response.status = Status.NotFound
      return null
    }
    val methodMatched = urlMatched.filter(r => r.method == request.method)
    if (methodMatched.isEmpty) {
      response.status = Status.MethodNotAllowed
      return null
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    })
    finalMatched
  }

  private def runRouterAction(it: Iterator[Route], request: Request, response: Response, pathMatcher: PathMatcher): Unit = {
    while (it.hasNext) {
      val router = it.next()
      val requestWithRouter = new RequestWithRouterParam(request, router, pathMatcher)
      Goa.putMessage(requestWithRouter -> response)
      if (!router.controller.isInstanceOf[Goa]) {
        router.controller.use { ctx =>
          val any = router.action()
          response.body = mapper.write(response, any)
          ctx.next()
        }
        router.controller.middlewareChain.messageReceived()
      } else {
        val any = router.action()
        response.body = mapper.write(response, any)
      }
      return
    }
  }

}
