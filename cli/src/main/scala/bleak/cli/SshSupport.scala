package bleak
package cli

import java.io.File
import java.nio.file.Paths

import com.jcraft.jsch._

trait SshSupport {

  private def currentJarFile(): File = {
    Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).toFile
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
      new SshClient(server, progressMonitor).uploadJar(currentJarFile().getAbsolutePath)
    }
  }
}