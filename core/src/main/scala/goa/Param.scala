package goa

import java.nio.charset.{Charset, StandardCharsets}

import goa.util.QueryStringDecoder

abstract class Param {

  def get(key: String): Option[String]

  def getAll(key: String): Iterable[String]

  def flat(): Map[String, String]

}

class RequestParam(val request: Request) extends Param {

  private[this] val getParams: Map[String, Array[String]] = {
    parseParams(request.uri)
  }

  private[this] val postParams: Map[String, Array[String]] = {
    if (request.mediaType.contains(MediaType.WwwForm)) {
      val encoding = request.charset.map(Charset.forName).getOrElse(StandardCharsets.UTF_8)
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

  def flat(): Map[String, String] = {
    postParams.map { x =>
      x._1 -> x._2.headOption.getOrElse("")
    } ++ getParams.map { x =>
      x._1 -> x._2.headOption.getOrElse("")
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

class RouterParam(paramMap: Param, params: Map[String, String]) extends Param {

  def get(key: String): Option[String] = {
    params.get(key) orElse paramMap.get(key)
  }

  override def getAll(key: String): Iterable[String] = {
    params.get(key).toIterable ++ paramMap.getAll(key)
  }

  override def flat(): Map[String, String] = paramMap.flat() ++ params
}