package bleak

trait Frame

sealed case class CloseFrame() extends Frame

sealed case class TextFrame(text: String) extends Frame

