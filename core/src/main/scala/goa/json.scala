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

  def stringify(any: Any)(implicit mapper: ObjectMapper = mapper): Buf = {
    Buf(mapper.writeValueAsBytes(any))
  }

  implicit class JsonSupportContext(val ctx: Context) {

    def json[T](implicit ctag: ClassTag[T], mapper: ObjectMapper = mapper): T = {
      mapper.readValue(ctx.request.body.string, ctag.runtimeClass.asInstanceOf[Class[T]])
    }

  }

  implicit class JsonSupportResponseBuilder(val builder: Response.Builder) {

    def json(body: Any): Response = {
      builder.contentType(MediaType.Json).body(Buf(mapper.writeValueAsBytes(body)))
    }
  }

}
