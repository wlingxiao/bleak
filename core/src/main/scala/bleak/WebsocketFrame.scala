package bleak

/**
  * Base class for websocket frame
  */
trait WebsocketFrame

/**
  * Web Socket Frame for closing the connection
  */
sealed case class CloseFrame(code: Int, reason: String) extends WebsocketFrame

/**
  * Web Socket text frame
  */
sealed case class TextFrame(text: String) extends WebsocketFrame

/**
  * Web Socket frame containing binary data
  */
sealed case class BinaryFrame(bytes: Array[Byte]) extends WebsocketFrame
