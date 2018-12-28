package bleak
package netty

import java.net.URI

import Status.{MethodNotAllowed, NotFound, Ok}
import logging.Logging
import netty.AttributeKeys._
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpMessage, HttpObjectAggregator, HttpRequest}

@Sharable
private[netty] class RouteHandler(app: Netty) extends SimpleChannelInboundHandler[HttpRequest](false) with Logging {
  override def channelRead0(ctx: ChannelHandlerContext, httpRequest: HttpRequest): Unit = {
    val (status, route) = findRoute(httpRequest)
    putResponse(ctx, status)
    putRoute(ctx, route)
    route match {
      case Some(value) =>
        ctx.pipeline()
          .addLast(new DefaultHttpObjectAggregator(value.maxContentLength))
          .addLast(new DispatchHandler(app))
      case None =>
        ctx.pipeline()
          .addLast(new DefaultHttpObjectAggregator(Int.MaxValue))
          .addLast(new DispatchHandler(app))
    }
    ctx.fireChannelRead(httpRequest)
  }

  private def findRoute(request: HttpRequest): (Status, Option[Route]) = {
    val path = new URI(request.uri()).getPath
    val method = Method(request.method().name())
    val logRequest = s"${method.name} $path"

    log.debug(s"Finding route for request: $logRequest")
    val pathMatcher = app.pathMatcher
    val urlMatched = app.routes.filter(r => pathMatcher.tryMatch(r.path, path))
    if (urlMatched.isEmpty) {
      log.warn(s"No route found for request: $logRequest")
      return NotFound -> None
    }
    val methodMatched = urlMatched.filter(r => r.methods.toSeq.contains(method))
    if (methodMatched.isEmpty) {
      return MethodNotAllowed -> None
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    })
    log.debug(s"Found route for request: $logRequest")
    Ok -> finalMatched.headOption
  }

  private def putRoute(ctx: ChannelHandlerContext, route: Option[Route]): Unit = {
    ctx.channel().attr(routeKey).set(route)
  }

  private def putResponse(ctx: ChannelHandlerContext, status: Status): Unit = {
    val res = NettyResponse(status = status, httpHeaders = new DefaultHttpHeaders())
    ctx.channel().attr(responseKey).set(res)
  }

  private def putApp(ctx: ChannelHandlerContext): Unit = {
    ctx.channel().attr(appKey).set(app)
  }

}

private[netty] class DefaultHttpObjectAggregator(maxContentLength: Int) extends HttpObjectAggregator(maxContentLength) {

  override def handleOversizedMessage(ctx: ChannelHandlerContext, oversized: HttpMessage): Unit = {
    oversized match {
      case httpRequest: HttpRequest =>
        putResponse(ctx, Status.RequestEntityTooLarge)
        ctx.fireChannelRead(httpRequest)
      case _ => super.handleOversizedMessage(ctx, oversized)
    }
  }

  private def putResponse(ctx: ChannelHandlerContext, status: Status): Unit = {
    val res = NettyResponse(status = status, httpHeaders = new DefaultHttpHeaders())
    ctx.channel().attr(responseKey).set(res)
  }

}