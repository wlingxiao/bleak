package goa

import goa.matcher.PathMatcher

private class RouterMiddleware(app: Application, pathMatcher: PathMatcher) extends Middleware {
  override def apply(ctx: Context): Unit = {
    val r = findMatchedRouter(request)
    r.action()
  }

  def findMatchedRouter(request: Request): Router = {
    val urlMatched = app.routers.filter(r => pathMatcher.canMatch(r.path, request.path))
    if (urlMatched.isEmpty) {
    }
    val methodMatched = urlMatched.filter(r => r.method.equalsIgnoreCase(request.method))
    if (methodMatched.isEmpty) {
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    }).head
    finalMatched
  }

}
