package bleak
package netty

import bleak.netty.AttributeKeys._
import bleak.util.Executions
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http._

import scala.concurrent.ExecutionContext

@Sharable
private[netty] class DispatchHandler(app: Netty) extends SimpleChannelInboundHandler[HttpRequest] {

  protected implicit val ec: ExecutionContext = Executions.directec

  private val responseWriter = new ResponseWriter

  override def channelRead0(ctx: ChannelHandlerContext, msg: HttpRequest): Unit = {
    val request = createRequest(ctx, msg)
    val response = getResponse(ctx)
    val pipeline = createPipeline()
    getRoute(ctx) match {
      case Some(value) =>
        value match {
          case ws: WebSocketRoute =>
            pipeline.append(new WebSocketMiddleware(ctx, msg.uri(), msg, app.pathMatcher))
            ctx.channel().attr(routeKey).set(null)
            ctx.channel().attr(webSocketRouteKey).set(ws)
          case _: HttpRoute =>
            pipeline.append(new ActionExecutionMiddleware)
          case _ => throw new IllegalStateException(s"Unknown type of Route: ${value.getClass.getName}")
        }
      case _ =>
    }
    val f = pipeline.received(request, response)
    responseWriter.write(ctx, f)
  }

  private def createPipeline(): DefaultPipeline = {
    val pipe = DefaultPipeline(app.sessionManager, app)
    for (m <- app.middlewares) {
      pipe.append(m())
    }
    pipe
  }

  private def createRequest(chCtx: ChannelHandlerContext, msg: HttpRequest): NettyRequest = {
    new NettyRequest(msg, chCtx, getRoute(chCtx).orNull, app.pathMatcher)
  }

  private def getResponse(ctx: ChannelHandlerContext): Response = {
    ctx.channel().attr(responseKey).get()
  }

  private def getRoute(ctx: ChannelHandlerContext): Option[Route] = {
    ctx.channel().attr(routeKey).get()
  }

}
