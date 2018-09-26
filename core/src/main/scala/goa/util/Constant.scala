package goa.util

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

trait Constant[T <: Constant[T]] extends Ordered[T] {

  private val uniquifier = Constant.uniqueIdGenerator.getAndIncrement()

  /**
    * Returns the unique number assigned to this [[Constant]]
    */
  def id: Int

  /**
    * Returns the name of this [[Constant]]
    */
  def name: String

  override def compare(that: T): Int = {
    if (this == that) {
      return 0
    }
    val returnCode = hashCode() - that.hashCode()
    if (returnCode != 0) {
      return returnCode
    }
    if (uniquifier < that.uniquifier) {
      return -1
    }
    if (uniquifier > that.uniquifier) {
      return 1
    }
    throw new IllegalArgumentException("failed to compare two different constants")
  }


  override def toString: String = name

}

object Constant {

  private val uniqueIdGenerator = new AtomicLong()

}

abstract class ConstantPool[T <: Constant[T]] {

  private val constants = new ConcurrentHashMap[String, T]()

  private val idGenerator = new AtomicInteger(1)

  private def nextId: Int = idGenerator.getAndIncrement

  private def getOrCreate(name: String): T = {
    var constant = constants.get(name)
    if (constant == null) {
      val tempConstant = newConstant(nextId, name)
      constant = constants.putIfAbsent(name, tempConstant)
      if (constant == null) return tempConstant
    }
    constant
  }

  private def createOrThrow(name: String): T = {
    var constant = constants.get(name)
    if (constant == null) {
      val tempConstant = newConstant(nextId, name)
      constant = constants.putIfAbsent(name, tempConstant)
      if (constant == null) return tempConstant
    }
    throw new IllegalArgumentException(String.format("'%s' is already in use", name))
  }

  /**
    * Returns the [[Constant]] which is assigned to the given name.
    * If there is no such [[Constant]], a new one will be created and returned.
    * Once created, the subsequent calls with the same name will always return the previously created one
    *
    * @param name the name of the [[Constant]]
    */
  def of(name: String): T = {
    getOrCreate(name)
  }

  /**
    * Creates a new [[Constant]] for the given name or throw an [[IllegalArgumentException]]
    * if a [[Constant]] for the given name exists
    */
  def newInstance(name: String): T = {
    createOrThrow(name)
  }

  /**
    * Returns `true` if a [[Constant]] exists for the given name
    */
  def exists(name: String): Boolean = {
    constants.containsKey(name)
  }

  protected def newConstant(id: Int, name: String): T

}