package bleak

import com.typesafe.scalalogging.Logger
import org.slf4j.LoggerFactory

trait LazyLogging {
  @transient
  protected lazy val log: Logger = Logger(LoggerFactory.getLogger(getClass.getName))
}
