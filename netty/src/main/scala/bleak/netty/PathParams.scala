package bleak
package netty

private[netty] class PathParams(paramMap: Params,
                                params: Map[String, String],
                                override val splat: Option[String]) extends Params {

  def get(key: String): Option[String] = {
    params.get(key) orElse paramMap.get(key)
  }

  override def getAll(key: String): Iterable[String] = {
    params.get(key) ++ paramMap.getAll(key)
  }
}
