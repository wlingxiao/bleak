package goa

abstract class Param {

  def get(key: String): Option[String]

  def getAll(key: String): Iterable[String]

}

class RequestParam(val request: Request) extends Param {

  private[this] val getParams: Map[String, Array[String]] = {
    parseParams(request.uri)
  }

  private[this] val postParams: Map[String, Array[String]] = {
    if (request.mediaType.contains(MediaType.WwwForm)) {
      parseParams("?" + request.contentString)
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
    val post = postParams.get(key)
    post match {
      case Some(x) => x
      case None =>
        getParams.get(key) match {
          case Some(x) => x
          case None => Nil
        }
    }
  }

  private def parseParams(uri: String): Map[String, Array[String]] = {
    QueryStringDecoder.decode(uri)
  }
}
