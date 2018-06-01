package goa

abstract class Response extends Message {

  def status: Status

  def status_=(newValue: Status): Unit

  def status(status: Status): this.type = {
    this.status = status
    this
  }

  override def toString: String = {
    s"""Response($status)"""
  }

}

private object Response {

  private class Impl extends Response {

    private[this] var _status = Status.Ok

    def status: Status = _status

    def status_=(status: Status): Unit = _status = status
  }

  def apply(): Response = {
    new Impl
  }

}
