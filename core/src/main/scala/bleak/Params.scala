package bleak

import scala.collection.mutable

/**
  * Request Parameter map.
  *
  * This is a multiple-map.
  *
  * Use `getAll()` to get all values for a key.
  */
trait Params[T] {

  def getAll(key: String): Iterable[T]

  def get(key: String): Option[T]

  def iterator: Iterator[(String, T)]

  def apply(key: String): T

}

trait PathParams extends Params[String] {
  def splat: Option[String]
}

trait Headers extends Params[String] with mutable.Map[String, String] {

  def add(k: String, v: String): Headers

  def set(k: String, v: String): Headers
}

trait QueryParams extends Params[String]

trait FormParams extends Params[String]

trait FormFileParams extends Params[FileBuf]
