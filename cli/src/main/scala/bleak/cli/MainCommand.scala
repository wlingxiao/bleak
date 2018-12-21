package bleak
package cli

import picocli.CommandLine
import picocli.CommandLine.{Model, Spec}

private class MainCommand extends Runnable {

  @CommandLine.Option(names = Array("-h", "--help"), description = Array("Show help"), usageHelp = true)
  var help: Boolean = _

  @CommandLine.Option(names = Array("-v", "--version"), description = Array("Show version"))
  var version: Boolean = _

  @CommandLine.Unmatched()
  var unmatched: Array[String] = _

  @Spec var spec: Model.CommandSpec = _

  def run(): Unit = {
    if (version) {
      printVersion()
    } else printHelp()
  }

  private def printVersion(): Unit = {
    System.err.println("Bleak-CLI 0.0.1")
  }

  private def printHelp(): Unit = {
    spec.commandLine().usage(System.err)
  }

}
