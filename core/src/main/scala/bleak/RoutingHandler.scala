package bleak

import java.net.{InetSocketAddress, URI}

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpRequest}
import io.netty.util.ReferenceCountUtil

@Sharable
private class RoutingHandler(app: Application)
    extends SimpleChannelInboundHandler[HttpRequest](false)
    with LazyLogging {
  override def channelRead0(ctx: ChannelHandlerContext, req: HttpRequest): Unit = {
    val (status, route) = findRoute(req)
    route match {
      case Some(value) =>
        ctx
          .pipeline()
          .addLast(new HttpObjectAggregator(Int.MaxValue))
          .addLast(new DispatchHandler(app, status, route))
      case None =>
        ctx
          .pipeline()
          .addLast(new HttpObjectAggregator(Int.MaxValue))
          .addLast(new DispatchHandler(app, status, route))
    }
    ReferenceCountUtil.retain(req)
    ctx.fireChannelRead(req)
  }

  private def findRoute(request: HttpRequest): (Int, Option[Route]) = {
    val path = new URI(request.uri()).getPath
    val method = request.method()
    val logRequest = s"${method.name} $path"

    log.debug(s"Finding route for request: $logRequest")
    val pathMatcher = app.pathMatcher
    val urlMatched = app.routes.filter(r => pathMatcher.tryMatch(app.basePath + r.path, path))
    if (urlMatched.isEmpty) {
      log.warn(s"No route found for request: $logRequest")
      return 404 -> None
    }
    val methodMatched = urlMatched.filter(r => r.method == method)
    if (methodMatched.isEmpty) {
      return 405 -> None
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    })
    log.debug(s"Found route for request: $logRequest")
    200 -> finalMatched.headOption
  }
}
