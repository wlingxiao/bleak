package bleak

trait WebSocketEvent

sealed case class Close() extends WebSocketEvent

sealed case class Text(text: String) extends WebSocketEvent

