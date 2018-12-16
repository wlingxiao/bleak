package bleak
package netty

case class DefaultFormFile(bytes: Array[Byte], fileName: String) extends FormFile
