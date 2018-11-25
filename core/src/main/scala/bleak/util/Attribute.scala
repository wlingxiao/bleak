package bleak.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicReference

trait Attribute[T] {

  /**
    * Returns the current value
    */
  def get: Option[T]

  /**
    * Set the value
    */
  def set(value: T): Unit

}

trait AttributeMap {

  import AttributeMap._

  private val map = new ConcurrentHashMap[String, Attribute[_]]()

  def attr[T](key: String): Attribute[T] = {
    if (map.containsKey(key)) {
      map.get(key).asInstanceOf[Attribute[T]]
    } else {
      val attr = new DefaultAttribute[T]
      map.put(key, attr)
      attr
    }
  }

}

object AttributeMap {

  private class DefaultAttribute[T] extends Attribute[T] {

    private[this] val ref = new AtomicReference[T]()

    override def get: Option[T] = {
      Option(ref.get())
    }

    override def set(value: T): Unit = ref.set(value)
  }

}
