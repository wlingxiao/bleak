package bleak
package cli

import java.io.File
import java.nio.file.Paths

import com.jcraft.jsch._

trait SshSupport {

  protected def currentJarFile(): File = {
    Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).toFile
  }

  protected def currentJarFilename: String = {
    currentJarFile().getName
  }

  protected def progressMonitor: SftpProgressMonitor = {
    new SftpProgressMonitor {
      override def init(op: Int, src: String, dest: String, max: Long): Unit = {
        println(s"Transfer file from $src to $dest")
      }

      override def count(count: Long): Boolean = {
        true
      }

      override def end(): Unit = {
        println("Transfer finished")
      }
    }
  }

  def servers: Seq[Server] = Nil

  def batchUpload(): Unit = {
    for (server <- servers) {
      new SshClient(server, progressMonitor).put(currentJarFile().getAbsolutePath, server.remotePath)
    }
  }

  def start(): Unit = {
    for (server <- servers) {
      val sshClient = new SshClient(server, progressMonitor)
      val started = processStarted(sshClient)
      if (started.isEmpty) {
        val logFile = s"${server.remotePath + "/" + "log.out"}"
        val cmd = s"java -jar ${server.remotePath + "/" + currentJarFilename} run > $logFile 2>&1 &"
        println(s"Starting ${server.remotePath + "/" + currentJarFilename}")
        sshClient.exec(cmd)
        println(s"Start succeeded")
      } else {
        println(s"$currentJarFilename is started. pid is ${started.get}")
      }
    }
  }

  def processStarted(client: SshClient): Option[String] = {
    val cmdRets = client.exec(s"jps -l | grep $currentJarFilename").split("\r")
    for (line <- cmdRets) {
      val cmds = line.split(" ")
      if (cmds.length == 2) {
        val filename = cmds(1).trim
        if (filename.endsWith(currentJarFilename)) {
          val jpid = cmds(0).trim
          return Some(jpid)
        }
      }
    }
    None
  }

  def stop(): Unit = {
    for (server <- servers) {
      val client = new SshClient(server, progressMonitor)
      val cmdRets = client.exec(s"jps -l | grep $currentJarFilename").split("\r")
      for (line <- cmdRets) {
        val cmds = line.split(" ")
        if (cmds.length == 2) {
          val filename = cmds(1).trim
          if (filename.endsWith(currentJarFilename)) {
            val jpid = cmds(0).trim
            println(s"Killing process $jpid")
            client.exec(s"kill $jpid")
            println(s"Kill succeeded")
          }
        }
      }
    }
  }

  def restart(): Unit = {
    stop()
    start()
  }

}