package bleak
package cli

import java.io.InputStream
import java.util.Properties

import com.jcraft.jsch._

import scala.collection.mutable

class SshClient(server: Server, progressMonitor: SftpProgressMonitor) {

  private val username = server.user

  private val host = server.host

  private val sshPort = server.port

  private val identities = server.identities

  private val config = {
    val properties = new Properties()
    for ((k, v) <- server.config) {
      properties.put(k, v)
    }
    properties
  }

  private val password = server.password

  def put(localFile: String, remotePath: String): Unit = {
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

  def exec(cmd: String): String = {
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
    var execChannel: ChannelExec = null
    try {
      session.connect()
      execChannel = session.openChannel("exec").asInstanceOf[ChannelExec]
      execChannel.setCommand(cmd)
      val in = execChannel.getInputStream
      execChannel.connect()

      val sb = new mutable.StringBuilder()
      while (!execChannel.isClosed) {
        read(in, sb)
      }
      read(in, sb)
      sb.toString()
    } finally {
      if (execChannel != null) {
        execChannel.disconnect()
      }
      if (session != null && session.isConnected) {
        session.disconnect()
      }
    }
  }

  private def read(in: InputStream, out: mutable.StringBuilder): Unit = {
    val buf = new Array[Byte](1024)
    while (in.available() > 0) {
      val i = in.read(buf, 0, 1024)
      out.append(new String(buf, 0, i))
    }
  }

}
