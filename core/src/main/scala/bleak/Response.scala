package bleak

abstract class Response extends Message {

  def status: Status

  def status_=(status: Status): Unit

  override def toString: String =
    s"""Response($status)"""

}
