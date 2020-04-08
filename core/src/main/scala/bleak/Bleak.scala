package bleak

class Bleak extends Application with Server

object Bleak {
  def apply(): Bleak = new Bleak
}
