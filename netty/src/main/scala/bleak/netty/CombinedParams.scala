package bleak
package netty

private[netty] class CombinedParams(request: Request) extends Params[String] {
  override def getAll(key: String): Iterable[String] = {
    val pathParams = request.paths
    val queryParams = request.query
    val formParams = request.form
    pathParams.getAll(key) ++ queryParams.getAll(key) ++ formParams.getAll(key)
  }

  override def get(key: String): Option[String] = {
    getAll(key).headOption
  }

  override def iterator: Iterator[(String, String)] = {
    val pathParams = request.paths
    val queryParams = request.query
    val formParams = request.form
    pathParams.iterator ++ queryParams.iterator ++ formParams.iterator
  }

  override def apply(key: String): String = get(key).orNull
}
