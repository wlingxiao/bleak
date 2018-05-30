package goa

final class Method private(val name: String) {
  override def toString: String = name

  override def equals(other: Any): Boolean = other match {
    case other: Method => (this eq other) || this.name == other.name
    case _ => false
  }

  override def hashCode(): Int = name.hashCode
}

object Method {

  val Get: Method = new Method("GET")
  val Post: Method = new Method("POST")
  val Put: Method = new Method("PUT")
  val Head: Method = new Method("HEAD")
  val Patch: Method = new Method("PATCH")
  val Delete: Method = new Method("DELETE")
  val Trace: Method = new Method("TRACE")
  val Connect: Method = new Method("CONNECT")
  val Options: Method = new Method("OPTIONS")

  def apply(name: String): Method = name.toUpperCase match {
    case "GET" => Get
    case "POST" => Post
    case "PUT" => Put
    case "HEAD" => Head
    case "PATCH" => Patch
    case "DELETE" => Delete
    case "TRACE" => Trace
    case "CONNECT" => Connect
    case "OPTIONS" => Options
    case _ => new Method(name)
  }
}
