package bleak

import java.io.File

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

    implicit object FileConverter extends Converter[File] {
      override def apply(file: File): Result = {
        Result(Ok, FileBuf(file), Map.empty, Seq.empty)
      }
    }

  }

  implicit def any2Result[T](any: T)(implicit converter: Converter[T]): Result = converter(any)

}
