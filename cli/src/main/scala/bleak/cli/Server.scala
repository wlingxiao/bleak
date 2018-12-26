package bleak
package cli

case class Server(user: String,
                  host: String,
                  remotePath: String,
                  password: String = "",
                  port: Int = 22,
                  identities: Iterable[Identity] = Seq(Identity.userHomeIdentity),
                  config: Map[String, String] = Server.defaultConfig) {
}

object Server {

  val defaultConfig: Map[String, String] = {
    Map("StrictHostKeyChecking" -> "no")
  }

}