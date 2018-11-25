package bleak

import bleak.Route.Attribute
import reflect.{ClassTag, classTag}

case class Route(path: String, method: Method, name: String, attrs: Map[Class[_ <: Attribute], Attribute]) {

  def attr[T <: Attribute : ClassTag]: Option[T] = {
    val clazz = classTag[T].runtimeClass.asInstanceOf[Class[T]]
    attrs.get(clazz).asInstanceOf[Option[T]]
  }

  type Action = Context => Result

  var action: Action = _

  def apply(ac: Action): Route = {
    action = ac
    this
  }

  def apply(ret: => Result): Route = {
    action = _ => ret
    this
  }

}

case class Result(status: Status, body: Buf, headers: Map[String, String], cookies: Seq[Cookie])

object Result {

  trait Converter[-T] {
    def apply(any: T): Result
  }

  object Converter {

    implicit object AnyValConverter extends Converter[AnyVal] {
      override def apply(value: AnyVal): Result = {
        value match {
          case _: Unit => Result(Ok, null, Map.empty, Seq.empty)
          case _ => Result(Ok, Buf(value.toString.getBytes()), Map.empty, Seq.empty)
        }
      }
    }

    implicit object StringConverter extends Converter[String] {
      override def apply(str: String): Result = {
        Result(Ok, Buf(str.toString.getBytes()), Map.empty, Seq.empty)
      }
    }

    implicit object ByteArrayConverter extends Converter[Array[Byte]] {
      override def apply(bytes: Array[Byte]): Result = {
        Result(Ok, Buf(bytes), Map(Fields.ContentLength -> bytes.length.toString), Seq.empty)
      }
    }

  }

  implicit def any2Result[T](any: T)(implicit converter: Converter[T]): Result = converter(any)

}

object Route {

  trait Attribute

  case class Consume(value: String*) extends Attribute

  case class Produce(value: String*) extends Attribute

  case class Charset(value: String) extends Attribute

}
