package goa

import java.util.concurrent.atomic.AtomicReference

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.reflect.ClassTag

object json {

  private val DefaultMapper = {
    val mapper = new ObjectMapper
    mapper.registerModule(DefaultScalaModule)
    mapper.setSerializationInclusion(Include.NON_NULL)
    mapper
  }

  private val mapperRef = new AtomicReference[ObjectMapper](DefaultMapper)

  def mapper(om: ObjectMapper): Unit = {
    mapperRef.set(om)
  }

  def mapper: ObjectMapper = {
    mapperRef.get()
  }

  def parse[T](ctx: Context)(implicit ctag: ClassTag[T], mapper: ObjectMapper = mapper): T = {
    mapper.readValue(ctx.request.body.string, ctag.runtimeClass.asInstanceOf[Class[T]])
  }

  def apply(any: AnyRef)(implicit mapper: ObjectMapper = mapper): Buf = {
    Buf(mapper.writeValueAsBytes(any))
  }

}
