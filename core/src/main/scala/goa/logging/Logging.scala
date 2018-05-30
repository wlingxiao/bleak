package goa.logging

private[goa] trait Logging {

  protected lazy val log: Logger = Loggers.getLogger(this.getClass)

}

private[goa] abstract class Logger {

  def debug(msg: => String): Unit

  def error(msg: => String, t: => Throwable): Unit

  def info(msg: => String): Unit

}
