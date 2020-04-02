package bleak

import io.netty.handler.codec.http.QueryStringDecoder
import scala.jdk.CollectionConverters._

/**
  * Request Parameter map.
  *
  * This is a multiple-map.
  *
  * Use `getAll()` to get all values for a key.
  */
trait Params {

  def getAll(key: String): Iterable[String]

  def get(key: String): Option[String]

}

object Params {

  class Query(uri: String) extends Params {

    private[this] val decodedParams = new QueryStringDecoder(uri).parameters()

    override def getAll(key: String): Iterable[String] = decodedParams.get(key).asScala

    override def get(key: String): Option[String] =
      getAll(key).headOption
  }

}
