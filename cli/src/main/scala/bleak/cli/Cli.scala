package bleak
package cli

import picocli.CommandLine
import picocli.CommandLine._
import java.util.{List => JList}

trait Cli extends SshSupport {
  def app: Application
}

object Cli {

  def run(cli: Cli, args: Array[String]): Unit = {
    val mainCommand = new MainCommand()
    val deployCommand = new DeployCommand(cli)
    val runCommand = new RunCommand(cli)
    try {
      val cmdLine = new CommandLine(mainCommand)
      cmdLine.addSubcommand("deploy", deployCommand)
      cmdLine.addSubcommand("run", runCommand)
      cmdLine.setSeparator(" ")
      cmdLine.parseWithHandlers(parseResultHandler, exceptionHandler, args: _*)
    } catch {
      case e: ParameterException =>
        e.getCommandLine.usage(System.err)
    }
  }

  private def parseResultHandler: IParseResultHandler2[JList[AnyRef]] = {
    new RunLast().useOut(System.out).useErr(System.err).useAnsi(Help.Ansi.AUTO)
  }

  private def exceptionHandler: IExceptionHandler2[JList[AnyRef]] = {
    new DefaultExceptionHandler[java.util.List[Object]].useErr(System.err).useAnsi(Help.Ansi.AUTO)
  }

}