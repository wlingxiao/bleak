package bleak
package netty

import java.net.{InetSocketAddress, URI}

import bleak.Status.MethodNotAllowed
import bleak.logging.Logging
import bleak.matcher.PathMatcher
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpRequest

@Sharable
private class RoutingHandler(app: Application)
    extends SimpleChannelInboundHandler[HttpRequest](false)
    with Logging {
  override def channelRead0(ctx: ChannelHandlerContext, req: HttpRequest): Unit = {
    val (status, route) = findRoute(req)
    route match {
      case Some(value) =>
        ctx
          .pipeline()
          .addLast(new OversizedMessageHandler(Int.MaxValue, app, route))
          .addLast(new DispatchHandler(app, status, route))
      case None =>
        ctx
          .pipeline()
          .addLast(new OversizedMessageHandler(Int.MaxValue, app, route))
          .addLast(new DispatchHandler(app, status, route))
    }
    ctx.fireChannelRead(req)
  }

  private def findRoute(request: HttpRequest): (Status, Option[Route[_, _]]) = {
    val path = new URI(request.uri()).getPath
    val method = Method(request.method().name())
    val logRequest = s"${method.name} $path"

    log.debug(s"Finding route for request: $logRequest")
    val pathMatcher = app.pathMatcher
    val urlMatched = app.routes.filter(r => pathMatcher.tryMatch(app.basePath + r.path, path))
    if (urlMatched.isEmpty) {
      log.warn(s"No route found for request: $logRequest")
      return Status.NotFound -> None
    }
    val methodMatched = urlMatched.filter(r => r.methods.toSeq.contains(method))
    if (methodMatched.isEmpty) {
      return MethodNotAllowed -> None
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    })
    log.debug(s"Found route for request: $logRequest")
    Status.Ok -> finalMatched.headOption
  }
}
