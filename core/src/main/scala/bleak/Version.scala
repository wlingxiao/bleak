package bleak

/**
  * The version of http
  */
final case class Version private (major: Int, minor: Int) {

  /**
    * Returns the full protocol version text such as "HTTP/1.1"
    */
  override def toString: String = s"HTTP/$major.$minor"
}

object Version {

  /**
    * HTTP/1.0
    */
  val Http10 = Version(1, 0)

  /**
    * HTTP/1.1
    */
  val Http11 = Version(1, 1)
}
