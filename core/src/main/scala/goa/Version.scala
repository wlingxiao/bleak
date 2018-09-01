package goa

final case class Version private(major: Int, minor: Int) {

  val versionString: String = s"HTTP/$major.$minor"

  override def toString: String = versionString
}

object Version {

  val Http10: Version = Version(1, 0)

  val Http11: Version = Version(1, 1)
}
