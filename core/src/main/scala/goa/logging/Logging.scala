package goa.logging

private[goa] trait Logging {

  protected[goa] lazy val log: Logger = Loggers.getLogger(this.getClass)

}

private[goa] abstract class Logger {

  def trace(msg: => String): Unit

  def trace(msg: => String, t: Throwable): Unit

  def debug(msg: => String): Unit

  def debug(msg: => String, t: Throwable): Unit

  def info(msg: => String): Unit

  def info(msg: => String, t: Throwable): Unit

  def warn(msg: => String): Unit

  def warn(msg: => String, t: Throwable): Unit

  def error(msg: String): Unit

  def error(msg: => String, t: => Throwable): Unit

}
