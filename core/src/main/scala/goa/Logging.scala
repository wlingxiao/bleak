package goa

import org.slf4j.{LoggerFactory, Logger => Slf4JLogger}

private[goa] trait Logging {

  protected lazy val log = Logger(this)

}

private[goa] abstract class Logger {

  def debug(msg: => String): Unit

  def error(msg: => String, t: => Throwable): Unit

  def info(msg: => String): Unit

}

private[goa] object Logger {

  private class Slf4JUnderlying(underlying: Slf4JLogger) extends Logger {
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

  def apply(logging: Logging): Logger = {
    new Slf4JUnderlying(LoggerFactory.getLogger(logging.getClass))
  }
}