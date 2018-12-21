package bleak
package cli

import picocli.CommandLine
import picocli.CommandLine.Command

@Command(name = "run", description = Array("Run Server"))
private class RunCommand(cli: Cli) extends Runnable {

  @CommandLine.Option(
    names = Array("-H", "--host"),
    description = Array("Set host         [default: localhost]"))
  var host: String = _

  @CommandLine.Option(
    names = Array("-p", "--port"),
    description = Array("Set port         [default: 3000]"))
  var port: Int = Int.MinValue

  @CommandLine.Option(
    names = Array("-h", "--help"),
    description = Array("Show help"), usageHelp = true)
  var help: Boolean = _

  @CommandLine.Unmatched()
  var unmatched: Array[String] = _

  def run(): Unit = {
    runApp()
  }

  private def app: Application = cli.app

  private def runApp(): Unit = {
    if (host != null && host.nonEmpty && port != Int.MinValue) {
      app.run(host = this.host, port = this.port)
    } else if (host != null && host.nonEmpty) {
      app.run(host = this.host)
    } else if (port != Int.MinValue) {
      app.run(port = this.port)
    } else {
      app.run()
    }
  }

}
