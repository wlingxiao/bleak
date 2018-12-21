package bleak
package cli

import picocli.CommandLine
import picocli.CommandLine.Command

@Command(name = "deploy", description = Array("Deploy jar file"))
private class DeployCommand(sshClient: SshSupport) extends Runnable {

  @CommandLine.Option(names = Array("-h", "--help"), description = Array("Show help"), usageHelp = true)
  var help: Boolean = _

  @CommandLine.Unmatched()
  var unmatched: Array[String] = _

  def run(): Unit = {
    sshClient.batchUpload()
  }
}
