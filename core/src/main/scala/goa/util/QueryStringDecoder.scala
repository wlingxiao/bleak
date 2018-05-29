package goa.util

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

import scala.annotation.tailrec
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

private[goa] object QueryStringDecoder {

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
