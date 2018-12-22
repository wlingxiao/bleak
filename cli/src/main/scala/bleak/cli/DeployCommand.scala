package bleak
package cli

import picocli.CommandLine
import picocli.CommandLine.Command

@Command(name = "deploy", description = Array("Deploy jar file and restart remote process"))
private class DeployCommand(sshClient: SshSupport) extends Runnable {

  @CommandLine.Option(names = Array("--start"), description = Array("Start remote process"))
  var start: Boolean = _

  @CommandLine.Option(names = Array("--stop"), description = Array("Stop remote process"))
  var stop: Boolean = _

  @CommandLine.Option(names = Array("--restart"), description = Array("Stop remote process"))
  var restart: Boolean = _

  @CommandLine.Option(names = Array("--upload"), description = Array("Upload file to remote server"))
  var upload: Boolean = _

  @CommandLine.Option(names = Array("-h", "--help"), description = Array("Show help"), usageHelp = true)
  var help: Boolean = _

  @CommandLine.Unmatched()
  var unmatched: Array[String] = _

  def run(): Unit = {
    if (start) {
      sshClient.start()
    } else if (stop) {
      sshClient.stop()
    } else if (restart) {
      sshClient.restart()
    } else if (upload) {
      sshClient.batchUpload()
    } else {
      sshClient.batchUpload()
      sshClient.restart()
    }
  }
}
