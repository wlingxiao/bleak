package goa.util

import scala.concurrent.ExecutionContext

object Executions {

  val directec: ExecutionContext = new ExecutionContext {

    def execute(runnable: Runnable): Unit = runnable.run()

    def reportFailure(t: Throwable): Unit = {
      throw t
    }
  }
}
