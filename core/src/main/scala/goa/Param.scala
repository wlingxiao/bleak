package goa

import java.nio.charset.{Charset, StandardCharsets}

import goa.util.{BufferUtils, QueryStringDecoder}

abstract class Param {

  def get(key: String): Option[String]

  def getAll(key: String): Iterable[String]

  def toMap: Map[String, String]
}

private[goa] class RequestParam(queryParam: QueryStringParam, bodyParam: RequestBodyParam) extends Param {

  def get(key: String): Option[String] = {
    queryParam.get(key).orElse(bodyParam.get(key))
  }

  def toMap: Map[String, String] = queryParam.toMap ++ bodyParam.toMap

  def getAll(key: String): Iterable[String] = {
    val it = queryParam.getAll(key)
    if (it.isEmpty) {
      bodyParam.getAll(key)
    } else it
  }
}

private[goa] class QueryStringParam(request: Request) extends Param {
  private[this] val queryParam: Map[String, Array[String]] = {
    parseParams(request.uri)
  }

  override def get(key: String): Option[String] = queryParam.get(key).flatMap(_.headOption)

  override def getAll(key: String): Iterable[String] = {
    val array: Array[String] = queryParam.getOrElse(key, Array.empty[String])
    array
  }

  override def toMap: Map[String, String] = {
    queryParam.map { x =>
      x._1 -> x._2.headOption.getOrElse("")
    }
  }

  private def parseParams(uri: String): Map[String, Array[String]] = {
    QueryStringDecoder.decode(uri)
  }
}

/**
  * decode application/x-www-form-urlencoded body
  */
private[goa] class RequestBodyParam(request: Request) extends Param {
  private[this] val bodyParam: Map[String, Array[String]] = {
    if (request.mediaType.contains(MediaType.WwwForm)) {
      val encoding = request.charset.map(Charset.forName).getOrElse(StandardCharsets.UTF_8)
      val contentString = BufferUtils.bufferToString(request.body, encoding)
      parseParams("?" + contentString)
    } else Map.empty
  }

  override def get(key: String): Option[String] = {
    bodyParam.get(key).flatMap(_.headOption)
  }

  override def getAll(key: String): Iterable[String] = {
    val array: Array[String] = bodyParam.getOrElse(key, Array.empty[String])
    array
  }

  override def toMap: Map[String, String] = {
    bodyParam.map { x =>
      x._1 -> x._2.headOption.getOrElse("")
    }
  }

  private def parseParams(uri: String): Map[String, Array[String]] = {
    QueryStringDecoder.decode(uri)
  }
}

private[goa] class RoutePathParam(param: Param, params: Map[String, String]) extends Param {

  def get(key: String): Option[String] = {
    params.get(key) orElse param.get(key)
  }

  override def getAll(key: String): Iterable[String] = {
    params.get(key).toIterable ++ param.getAll(key)
  }

  override def toMap: Map[String, String] = param.toMap ++ params
}