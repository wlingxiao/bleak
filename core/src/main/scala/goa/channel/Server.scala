package goa.channel

trait Server {

  def start(host: String, port: Int): Unit

  def join(): Unit

  def stop(): Unit

}
