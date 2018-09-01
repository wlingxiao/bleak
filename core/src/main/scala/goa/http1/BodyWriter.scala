package goa.http1

import java.nio.ByteBuffer

trait BodyWriter {

  type Finished

  def write(buffer: ByteBuffer): Unit

  def flush(): Unit

  def close(): Finished
}

