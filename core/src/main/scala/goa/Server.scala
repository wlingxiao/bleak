package goa

trait Server {

  val Host: String = "127.0.0.1"

  val Port: Int = 7865

  def start(): Unit

  def close(): Unit

}
