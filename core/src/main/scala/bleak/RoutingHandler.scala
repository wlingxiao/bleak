package bleak

import java.net.{InetSocketAddress, URI}

import bleak.Status.MethodNotAllowed
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.HttpRequest

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

private object RoutingHandler {

  private[blaze] class RouteRequest(
      ctx: ChannelHandlerContext,
      val request: Request,
      val matchedRoute: Option[Route[_, _]],
      app: Application)
      extends Request.Proxy {

    override def remoteAddress: InetSocketAddress =
      ctx.channel().remoteAddress() match {
        case inet: InetSocketAddress => inet
        case _ => super.remoteAddress
      }

    override def localAddress: InetSocketAddress =
      ctx.channel().localAddress() match {
        case inet: InetSocketAddress => inet
        case _ => super.remoteAddress
      }

    override def paths: PathParams =
      route
        .map(r => PathParams(app.basePath + r.path, path, app.pathMatcher))
        .getOrElse(PathParams.empty)
  }

}
