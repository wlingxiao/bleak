package bleak.logging

private[bleak] trait Logging {

  protected[bleak] lazy val log: Logger = Loggers.getLogger(this.getClass)

}

private[bleak] abstract class Logger {

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
