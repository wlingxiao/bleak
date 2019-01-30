package bleak

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

object Params {

  def apply(request: Request): Params[String] = new CombinedParams(request)

  private class CombinedParams(request: Request) extends Params[String] {
    override def getAll(key: String): Iterable[String] = {
      val pathParams = request.paths
      val queryParams = request.query
      val formParams = request.form
      pathParams.getAll(key) ++ queryParams.getAll(key) ++ formParams.getAll(key)
    }

    override def get(key: String): Option[String] =
      getAll(key).headOption

    override def iterator: Iterator[(String, String)] = {
      val pathParams = request.paths
      val queryParams = request.query
      val formParams = request.form
      pathParams.iterator ++ queryParams.iterator ++ formParams.iterator
    }
    override def apply(key: String): String = get(key).orNull
  }

  trait EmptyParams[T] extends Params[T] {
    override def getAll(key: String): Iterable[T] = Nil
    override def get(key: String): Option[T] = None
    override def iterator: Iterator[(String, T)] = Iterator.empty
    override def apply(key: String): T = null.asInstanceOf[T]
  }
}
