package goa.channel

trait Server {

  /**
    * Start the server which is bound to the specified host and port
    */
  def start(host: String, port: Int): Unit

  /**
    * Blocks current server
    */
  def join(): Unit

  /**
    * Immediately shutdown the [[Server]] instance
    */
  def stop(): Unit

}
