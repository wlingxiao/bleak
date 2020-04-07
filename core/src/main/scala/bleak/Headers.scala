package bleak

import io.netty.handler.codec.http.{DefaultHttpHeaders, HttpHeaders}

import scala.jdk.CollectionConverters._

trait Headers {

  def getAll(name: CharSequence): Iterable[String]

  def get(name: CharSequence): Option[String]

  def iterator: Iterator[(String, String)]

  def add(name: CharSequence, value: Any): Headers

  def add(name: CharSequence, values: Iterable[_]): Headers

  def set(name: CharSequence, value: Any): Headers

  def remove(name: CharSequence): Headers

  def contains(name: CharSequence, value: CharSequence, ignoreCase: Boolean): Boolean

}

object Headers {

  class Impl(val httpHeaders: HttpHeaders) extends Headers {

    override def getAll(name: CharSequence): Iterable[String] = httpHeaders.getAll(name).asScala

    override def get(name: CharSequence): Option[String] = Option(httpHeaders.get(name))

    override def iterator: Iterator[(String, String)] =
      httpHeaders.iteratorAsString().asScala.map(e => (e.getKey, e.getValue))

    override def add(name: CharSequence, value: Any): Headers = {
      httpHeaders.add(name, value)
      this
    }

    override def add(name: CharSequence, values: Iterable[_]): Headers = {
      httpHeaders.add(name, values.asJava)
      this
    }

    override def remove(name: CharSequence): Headers = {
      httpHeaders.remove(name)
      this
    }

    override def set(name: CharSequence, value: Any): Headers = {
      httpHeaders.set(name, value)
      this
    }

    override def contains(name: CharSequence, value: CharSequence, ignoreCase: Boolean): Boolean =
      httpHeaders.contains(name, value, ignoreCase)

    override def toString: String = httpHeaders.toString
  }

  def apply(httpHeaders: HttpHeaders): Headers = new Impl(httpHeaders)

  def apply(kv: (CharSequence, Any)*): Headers = {
    val httpHeaders = new DefaultHttpHeaders()
    for ((k, v) <- kv) {
      httpHeaders.add(k, v.toString)
    }
    apply(httpHeaders)
  }

  def empty: Headers = apply(new DefaultHttpHeaders())

}
