package goa

import goa.logging.Logging

class AccessLogMiddleware extends Middleware with Logging {
  override def apply(ctx: Context): Unit = {
    ctx.next()
    log.info(s""""${request.userAgent.getOrElse("")}" "${request.method.name.toUpperCase} ${request.uri} ${request.version}" ${response.status}""")
  }
}
