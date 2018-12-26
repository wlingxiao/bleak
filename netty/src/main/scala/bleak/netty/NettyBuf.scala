package bleak
package netty

import java.nio.charset.Charset

class NettyBuf(val bytes: Array[Byte], charset: Charset) extends Buf {
  override def string: String = new String(bytes, charset)
}
