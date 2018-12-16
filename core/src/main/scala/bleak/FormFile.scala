package bleak

trait FormFile {

  def bytes: Array[Byte]

  def fileName: String

}
