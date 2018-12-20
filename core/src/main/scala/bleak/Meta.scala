package bleak

trait Meta {

}

object Meta {

  case class Consume(value: String*) extends Meta

  case class Produce(value: String*) extends Meta

  case class Charset(value: String) extends Meta

}