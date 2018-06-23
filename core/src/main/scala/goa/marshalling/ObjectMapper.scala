package goa.marshalling

import java.io.InputStream
import java.nio.ByteBuffer

import com.fasterxml.jackson.databind.util.ByteBufferBackedInputStream
import com.fasterxml.jackson.databind.{ObjectMapper => JacksonObjectMapper}
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import scala.reflect.ClassTag
import scala.reflect.classTag

trait MyScalaObjectMapper extends ScalaObjectMapper {
  self: JacksonObjectMapper =>
}

class ObjectMapper(mapper: JacksonObjectMapper) {

  def parse[T: ClassTag](byteBuffer: ByteBuffer): T = {
    val is = new ByteBufferBackedInputStream(byteBuffer)
    mapper.readValue[T](is, classTag[T].runtimeClass.asInstanceOf[Class[T]])
  }

  def writeValueAsBytes(any: Any): Array[Byte] = {
    mapper.writeValueAsBytes(any)
  }

  def writeValueAsByteBuffer(any: Any): ByteBuffer = {
    ByteBuffer.wrap(writeValueAsBytes(any))
  }
}
