package bleak
import io.netty.handler.codec.http.QueryStringDecoder
import scala.collection.JavaConverters._
trait QueryParams extends Params[String]

object QueryParams {
  def empty: QueryParams = new QueryParams with Params.EmptyParams[String]
  def apply(uri: String): QueryParams = new Impl(uri)
  final class Impl(uri: String) extends QueryParams {
    private[this] val decodedParameters = new QueryStringDecoder(uri).parameters()

    override def getAll(key: String): Iterable[String] =
      Option(decodedParameters.get(key))
        .map(_.asScala)
        .getOrElse(Iterable.empty)

    override def get(key: String): Option[String] = getAll(key).headOption

    override def iterator: Iterator[(String, String)] = {
      val res = for {
        (key, value) <- decodedParameters.asScala
        v2 <- value.asScala
      } yield key -> v2
      res.iterator
    }

    override def apply(key: String): String = get(key).orNull
  }
}
