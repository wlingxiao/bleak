package bleak
package netty

private[netty] trait EmptyParams[T] extends Params[T] {
  override def getAll(key: String): Iterable[T] = Nil

  override def get(key: String): Option[T] = None

  override def iterator: Iterator[(String, T)] = Iterator.empty

  override def apply(key: String): T = null.asInstanceOf[T]
}
