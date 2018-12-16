package bleak
package netty

import io.netty.handler.codec.http.QueryStringDecoder

import scala.collection.JavaConverters._

private[netty] class DefaultQueryParams(uri: String) extends QueryParams {

  private val params = new QueryStringDecoder(uri).parameters()

  override def get(key: String): Option[String] = {
    getAll(key).headOption
  }

  override def getAll(key: String): Iterable[String] = {
    val data = params.get(key)
    if (data != null) {
      params.get(key).asScala
    } else Nil
  }

  override def iterator: Iterator[(String, String)] = {
    val res = for ((k, v) <- params.asScala; v2 <- v.asScala) yield {
      k -> v2
    }
    res.iterator
  }

  override def apply(key: String): String = get(key).orNull
}
