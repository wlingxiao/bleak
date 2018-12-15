package bleak
package netty

import io.netty.handler.codec.http.QueryStringDecoder
import collection.JavaConverters._

private[netty] class QueryParams(val request: Request) extends Params {

  private[this] val getParams: Map[String, Array[String]] = {
    parseParams(request.uri)
  }

  private[this] val postParams: Map[String, Array[String]] = {
    if (request.mimeType.contains(MediaType.WwwForm)) {
      val contentString = request.body.string
      parseParams("?" + contentString)
    } else {
      Map.empty
    }
  }

  def get(key: String): Option[String] = {
    val post = postParams.get(key)
    post match {
      case Some(x) =>
        x.headOption
      case None =>
        getParams.get(key) match {
          case Some(x) => x.headOption
          case None => None
        }
    }
  }

  def getAll(key: String): Iterable[String] = {
    getParams.getOrElse(key, Array.empty) ++ postParams.getOrElse(key, Array.empty)
  }

  override def splat: Option[String] = None

  private def parseParams(uri: String): Map[String, Array[String]] = {
    new QueryStringDecoder(uri).parameters().asScala.map { param =>
      param._1 -> param._2.asScala.toArray
    }.toMap
  }
}
