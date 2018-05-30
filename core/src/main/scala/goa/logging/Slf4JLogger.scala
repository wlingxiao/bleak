package goa.logging

private[goa] class Slf4JLogger(underlying: org.slf4j.Logger) extends Logger {

  override def debug(msg: => String): Unit = {
    if (underlying.isDebugEnabled) {
      underlying.debug(msg)
    }
  }

  override def error(msg: => String, t: => Throwable): Unit = {
    if (underlying.isErrorEnabled) {
      underlying.error(msg, t)
    }
  }

  override def info(msg: => String): Unit = {
    if (underlying.isInfoEnabled) {
      underlying.info(msg)
    }
  }

}
