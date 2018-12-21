package bleak.cli

import java.nio.file.Paths

case class Identity(priKey: String, pubkey: String)

object Identity {

  val userHomeIdentity: Identity = {
    val priKey = Paths.get(System.getProperty("user.home"), ".ssh", "id_rsa").toAbsolutePath.toString
    Identity(priKey, null)
  }

}