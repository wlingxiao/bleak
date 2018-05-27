package goa

import goa.matcher.PathMatcher

private class RouterMiddleware(app: Controller, pathMatcher: PathMatcher) extends Middleware {
  override def apply(ctx: Context): Unit = {
    val r = findMatchedRouter(request)
    if (r != null) {
      runRouterAction(r.iterator, request, response, pathMatcher)
    }
  }

  private def findMatchedRouter(request: Request): Seq[Router] = {
    val urlMatched = app.routers.filter(r => pathMatcher.canMatch(r.path, request.path))
    if (urlMatched.isEmpty) {
      response.status = 404
      response.reasonPhrase = "Not Found"
      return null
    }
    val methodMatched = urlMatched.filter(r => r.method.equalsIgnoreCase(request.method))
    if (methodMatched.isEmpty) {
      response.status = 415
      response.reasonPhrase = "Method Not Allowed"
      return null
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    })
    finalMatched
  }

  private def runRouterAction(it: Iterator[Router], request: Request, response: Response, pathMatcher: PathMatcher): Unit = {
    while (it.hasNext) {
      val router = it.next()
      val requestWithRouter = new RequestWithRouterParam(request, router, pathMatcher)
      Goa.putMessage(requestWithRouter -> response)
      if (router.controller != null) {
        router.controller.use { ctx =>
          router.action()
          ctx.next()
        }
        router.controller.middlewareChain.messageReceived()
      } else {
        router.action()
      }
      return
    }
  }

}
