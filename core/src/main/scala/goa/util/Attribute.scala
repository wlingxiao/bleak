package goa.util

import java.util.concurrent.atomic.{AtomicReference, AtomicReferenceArray, AtomicReferenceFieldUpdater}

/**
  * An attribute which allows to store a value reference.
  * It may be updated atomically and so is thread-safe
  *
  * @tparam T the type of the value it holds
  */
trait Attribute[T] {

  /**
    * Returns the key of this attribute
    */
  def key: AttributeKey[T]

  /**
    * Returns the current value, which may be null
    */
  def get: T

  /**
    * Set the value
    */
  def set(value: T): Unit

  /**
    * Atomically sets to given value and returns the old value which may be null if non was set before
    */
  def getAndSet(value: T): T

  /**
    * Atomically sets to the given value if this [[Attribute]]'s value if null.
    */
  def setIfAbsent(value: T): T

  /**
    * Atomically sets the value to the given updated value if the current value eq the expected value.
    */
  def compareAndSet(oldValue: T, newValue: T): Boolean

}

/**
  * Key which can be used to access [[Attribute]] out of the [[AttributeMap]]
  * Be ware that it is impossible to have multiple keys with the same name
  *
  * @tparam k the type of the [[Attribute]] which can be accessed via this [[AttributeKey]].
  *           'T' is used only at compile time
  */
final class AttributeKey[k](val id: Int, val name: String) extends Constant[AttributeKey[k]] {

  private val pool = AttributeKey.pool

  def valueOf[T](name: String): AttributeKey[T] = pool.of(name).asInstanceOf[AttributeKey[T]]

  def exists(name: String): Boolean = pool.exists(name)

  def newInstance[T](name: String): AttributeKey[T] = pool.newInstance(name).asInstanceOf[AttributeKey[T]]

}

object AttributeKey {

  private val pool = new ConstantPool[AttributeKey[Any]]() {
    override protected def newConstant(id: Int, name: String) = new AttributeKey[Any](id, name)
  }

  /**
    * Creates a new [[Constant]] for the given name or throw an [[IllegalArgumentException]]
    * if a [[Constant]] for the given name exists
    */
  def create[T](name: String): AttributeKey[T] = {
    pool.newInstance(name).asInstanceOf[AttributeKey[T]]
  }

  /**
    * Returns the [[Constant]] which is assigned to the given name.
    * If there is no such [[Constant]], a new one will be created and returned.
    * Once created, the subsequent calls with the same name will always return the previously created one
    *
    * @param name the name of the [[Constant]]
    */
  def of[T](name: String): AttributeKey[T] = {
    pool.of(name).asInstanceOf[AttributeKey[T]]
  }
}

/**
  * Holds [[Attribute]]s which can be accessed via [[AttributeKey]].
  * Implementations must be thread-safe.
  */
trait AttributeMap {

  /**
    * Get the [[Attribute]] for the given [[AttributeKey]].
    * This method will never return null, but may return an [[Attribute]] which does not have a value set yet.
    */
  def attr[T](key: AttributeKey[T]): Attribute[T]

  /**
    * Returns true if the [[Attribute]] exists in this [[AttributeMap]].
    */
  def hasAttr[T](key: AttributeKey[T]): Boolean

}

object AttributeMap {

  def apply(): AttributeMap = new Impl()

  class DefaultAttribute[T](head: DefaultAttribute[_], val key: AttributeKey[T]) extends AtomicReference[T] with Attribute[T] {


    var prev: DefaultAttribute[_] = _
    var next: DefaultAttribute[_] = _

    def this() {
      this(null, null)
    }

    override def setIfAbsent(value: T): T = {
      while (!compareAndSet(null.asInstanceOf[T], value)) {
        val old = get
        if (old != null) return old
      }
      null.asInstanceOf[T]
    }
  }

  private val BucketSize = 4

  private val Mask = BucketSize - 1

  /**
    * Default [[AttributeMap]] implementation which use simple synchronization per bucket to keep the memory overhead
    * as low as possible.
    */
  class Impl extends AttributeMap {

    private val updater = AtomicReferenceFieldUpdater.newUpdater(classOf[Impl], classOf[AtomicReferenceArray[_]], "attributes")

    private def index(key: AttributeKey[_]) = key.id & AttributeMap.Mask

    @volatile
    private var attributes: AtomicReferenceArray[DefaultAttribute[_]] = _

    def attr[T](key: AttributeKey[T]): Attribute[T] = {
      var attrs = attributes
      if (attrs == null) {
        attrs = new AtomicReferenceArray[DefaultAttribute[_]](BucketSize)
        if (!updater.compareAndSet(this, null, attrs)) {
          attrs = this.attributes
        }
      }
      val i = index(key)
      var head = attributes.get(i)
      if (head == null) {
        head = new DefaultAttribute()
        val attr = new DefaultAttribute[T](head, key)
        head.next = attr
        attr.prev = head
        if (attributes.compareAndSet(i, null, head)) {
          return attr
        } else {
          head = attributes.get(i)
        }
      }
      head.synchronized {
        var curr = head
        while (true) {
          val next = curr.next
          if (next == null) {
            val attr = new DefaultAttribute[T](head, key)
            curr.next = attr
            attr.prev = curr
            return attr
          }

          if (next.key == key) {
            return next.asInstanceOf[Attribute[T]]
          }
          curr = next
        }
      }
      null.asInstanceOf[Attribute[T]]
    }


    def hasAttr[T](key: AttributeKey[T]): Boolean = {
      val attributes = this.attributes
      if (attributes == null) {
        return false
      }
      val i = index(key)
      val head = attributes.get(i)
      if (head == null) {
        return false
      }
      head.synchronized {
        var curr = head.next
        while (curr != null) {
          if (curr.key == key) {
            return true
          }
          curr = curr.next
        }
        return false
      }
    }

  }

}
