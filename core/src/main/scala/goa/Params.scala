package goa

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
  * Request Parameter map.
  *
  * This is a multiple-map.
  *
  * Use `getAll()` to get all values for a key.
  */
trait Params {

  def get(key: String): Option[String]

  /**
    * Get all parameter for a key.
    */
  def getAll(key: String): Iterable[String]

  def splat: Option[String]

}

object Params {

  /**
    * Handle parameters in the URL and form encoded body.
    */
  class QueryParams(val request: Request) extends Params {

    private[this] val getParams: Map[String, Array[String]] = {
      parseParams(request.uri)
    }

    private[this] val postParams: Map[String, Array[String]] = {
      if (request.mediaType.contains(MediaType.WwwForm)) {
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
      QueryParamDecoder.decode(uri)
    }
  }

  object QueryParamDecoder {

    private[this] val CharsetName: String = StandardCharsets.UTF_8.name
    private[this] val MaxParams: Int = 1024

    def decode(uri: String): Map[String, Array[String]] = {
      val qPos = uri.indexOf('?')
      if (qPos < 0 || qPos == uri.length - 1) Map.empty
      else decodeParams(uri.substring(qPos + 1, uri.length))
    }

    private[this] def decodeParams(s: String): Map[String, Array[String]] = {
      val params = mutable.LinkedHashMap[String, ArrayBuffer[String]]()
      var nParams = 0
      var name: String = null
      var mark: Int = 0

      @tailrec
      def go(i: Int): Unit = {
        if (i < s.length && nParams < MaxParams) {
          val c = s.charAt(i)
          if (c == '=' && name == null) {
            if (mark != i) {
              name = decodeComponent(s.substring(mark, i))
            }
            mark = i + 1
          } else if (c == '&' || c == ';') {
            if (name == null && mark != i) {
              addParam(params, decodeComponent(s.substring(mark, i)), "")
              nParams += 1
            } else if (name != null) {
              addParam(params, name, decodeComponent(s.substring(mark, i)))
              nParams += 1
              name = null
            }
            mark = i + 1
          }
          go(i + 1)
        }
      }

      go(0)

      if (nParams == MaxParams) {
      } else if (mark != s.length) {
        if (name == null) {
          addParam(params, decodeComponent(s.substring(mark, s.length)), "")
        } else {
          addParam(params, name, decodeComponent(s.substring(mark, s.length)))
        }
      } else if (name != null) {
        addParam(params, name, "")
      }

      params.map(x => x._1 -> x._2.toArray).toMap
    }

    private[this] def addParam(params: mutable.Map[String, ArrayBuffer[String]], name: String, value: String): Unit = {
      val values = params.get(name) match {
        case None =>
          val list = new ArrayBuffer[String](1)
          params.put(name, list)
          list
        case Some(list) => list
      }
      values += value
    }

    private[this] def decodeComponent(s: String): String =
      URLDecoder.decode(s, CharsetName)
  }

}