package bleak

import java.net.URI

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{HttpMethod, HttpRequest}
import io.netty.util.{AttributeKey, ReferenceCountUtil}

@Sharable
private class RoutingHandler(app: Application)
    extends SimpleChannelInboundHandler[HttpRequest](false)
    with LazyLogging {

  import RoutingHandler._

  override def channelRead0(ctx: ChannelHandlerContext, req: HttpRequest): Unit = {
    ctx.channel().attr(RouteInfoAttrKey).set(findRoute(req))
    ctx
      .pipeline()
      .addLast(new DispatchHandler(app))
    ReferenceCountUtil.retain(req)
    ctx.fireChannelRead(req)
  }

  private def findRoute(request: HttpRequest): RouteInfo = {
    val path = new URI(request.uri()).getPath
    val method = request.method()
    log.debug(s"Finding Route: ${method.name} $path")
    val pathMatched = matchPath(path, method)
    if (pathMatched.isEmpty) {
      log.info(s"Route Not Found: ${method.name} $path")
      return RouteInfo(404, None)
    }
    val methodMatched = matchMethod(pathMatched, method)
    if (methodMatched.isEmpty) {
      return RouteInfo(405, None)
    }
    val finalMatched = methodMatched.sortWith(sortRoute(request, _, _))
    log.debug(s"Route Found: ${method.name} $path")
    RouteInfo(200, finalMatched.headOption)
  }

  private def matchPath(path: String, method: HttpMethod): Array[Route] =
    app.routes
      .filter(r => app.pathMatcher.tryMatch(app.basePath + r.path, path))

  private def matchMethod(pathMatched: Array[Route], method: HttpMethod): Array[Route] =
    pathMatched.filter(r => r.method == method)

  private def sortRoute(request: HttpRequest, x: Route, y: Route): Boolean =
    app.pathMatcher
      .getPatternComparator(request.uri)
      .compare(x.path, y.path) > 0

}

object RoutingHandler {
  val RouteInfoAttrKey: AttributeKey[RouteInfo] =
    AttributeKey.newInstance[RouteInfo](classOf[RouteInfo].getName)

  case class RouteInfo(status: Int, routeOpt: Option[Route])
}
