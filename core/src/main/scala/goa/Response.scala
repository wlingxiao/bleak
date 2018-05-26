package goa

abstract class Response extends Message {

  /**
    * Get status of this response
    */
  def status: Int

  /**
    * Set status of this response
    */
  def status_=(newValue: Int): Unit

  def status(status: Int): this.type = {
    this.status = status
    this
  }

  def reasonPhrase: String

  def reasonPhrase_=(reason: String): Unit

  def reasonPhrase(reason: String): this.type = {
    this.reasonPhrase = reason
    this
  }

  override def toString: String = {
    s"""Response($status)"""
  }

}

private object Response {

  private class Impl extends Response {

    private[this] var _status = 200

    private[this] var _reasonPhrase = "OK"

    def status: Int = _status

    def status_=(status: Int): Unit = _status = status

    override def reasonPhrase: String = _reasonPhrase

    override def reasonPhrase_=(reason: String): Unit = {
      _reasonPhrase = reason
    }
  }

  def apply(): Response = {
    new Impl
  }

}
