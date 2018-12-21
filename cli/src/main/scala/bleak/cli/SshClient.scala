package bleak
package cli

import java.util.Properties

import com.jcraft.jsch.{ChannelSftp, JSch, SftpProgressMonitor}

class SshClient(server: Server, progressMonitor: SftpProgressMonitor) {

  private val username = server.user

  private val host = server.host

  private val sshPort = server.port

  private val identities = server.identities

  private val remotePath = server.remotePath

  private val config = {
    val properties = new Properties()
    for ((k, v) <- server.config) {
      properties.put(k, v)
    }
    properties
  }

  private val password = server.password

  def uploadJar(localFile: String): Unit = {
    val jsch = new JSch
    val session = jsch.getSession(username, host, sshPort)
    if (password != null && password.nonEmpty) {
      session.setPassword(password)
    } else {
      for (i <- identities) {
        jsch.addIdentity(i.priKey, i.pubkey)
      }
    }
    session.setConfig(config)
    var sftp: ChannelSftp = null
    try {
      session.connect()
      sftp = session.openChannel("sftp").asInstanceOf[ChannelSftp]
      sftp.connect()
      sftp.put(localFile, remotePath, progressMonitor, ChannelSftp.OVERWRITE)
    } finally {
      if (sftp != null && sftp.isConnected) {
        sftp.disconnect()
      }
      if (session != null && session.isConnected) {
        session.disconnect()
      }
    }
  }

}
