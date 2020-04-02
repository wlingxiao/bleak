package bleak

import scala.concurrent.ExecutionContext

object Executions {

  implicit val directEc: ExecutionContext = new ExecutionContext {

    def execute(runnable: Runnable): Unit = runnable.run()

    def reportFailure(t: Throwable): Unit =
      throw t
  }

  implicit val global: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

}
