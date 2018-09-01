package goa

import scala.annotation.tailrec
import scala.collection.mutable

abstract class Headers extends mutable.Map[String, String] with mutable.MapLike[String, String, Headers] {

  def getAll(key: String): Seq[String]

  def getOrNull(key: String): String = get(key).orNull

  def add(k: String, v: String): Headers

  def set(k: String, v: String): Headers

  override def empty: Headers = Headers.empty
}

object Headers {

  def apply(headers: (String, String)*): Headers = {
    HeadersImpl(headers: _*)
  }

  def empty: Headers = apply()

}

private final class HeadersImpl extends Headers {

  import HeadersImpl._

  private val underlying = new HeaderMap

  override def getAll(key: String): Seq[String] = underlying.getAll(key)

  override def get(key: String): Option[String] = underlying.getFirst(key)

  override def add(k: String, v: String): Headers = {
    underlying.add(k, v)
    this
  }

  override def set(k: String, v: String): Headers = {
    underlying.set(k, v)
    this
  }

  override def +=(kv: (String, String)) = {
    val (k, v) = kv
    set(k, v)
    this
  }

  override def -=(key: String) = {
    underlying.removeAll(key)
    this
  }

  override def iterator = underlying.flattenIterator
}

private object HeadersImpl {

  def apply(headers: (String, String)*): Headers = {
    val result = new HeadersImpl
    headers.foreach(t => result.add(t._1, t._2))
    result
  }

  private final class HeaderMap extends mutable.HashMap[String, Header] {

    private def hashChar(c: Char): Int =
      if (c >= 'A' && c <= 'Z') c + 32
      else c

    override protected def elemHashCode(key: String): Int = {
      var result = 0
      var i = key.length - 1

      while (i >= 0) {
        val c = hashChar(key.charAt(i))
        result = 31 * result + c
        i = i - 1
      }

      result
    }

    override protected def elemEquals(key1: String, key2: String): Boolean =
      if (key1 eq key2) true
      else if (key1.length != key2.length) false
      else {
        @tailrec
        def loop(i: Int): Boolean =
          if (i == key1.length) true
          else {
            val a = key1.charAt(i)
            val b = key2.charAt(i)

            if (a == b || hashChar(a) == hashChar(b)) loop(i + 1)
            else false
          }

        loop(0)
      }

    def flattenIterator: Iterator[(String, String)] = new Iterator[(String, String)] {
      private[this] val it = entriesIterator
      private[this] var current: Header = _

      def hasNext: Boolean =
        it.hasNext || current != null

      def next(): (String, String) = {
        if (current == null) {
          current = it.next().value
        }

        val result = (current.name, current.value)
        current = current.next
        result
      }
    }

    def getFirst(key: String): Option[String] =
      get(key) match {
        case Some(h) => Some(h.value)
        case None => None
      }

    def getAll(key: String): Seq[String] =
      get(key) match {
        case Some(hs) => hs.values
        case None => Nil
      }

    def add(key: String, value: String): Unit = {
      val h = new Header(key, value)
      get(key) match {
        case Some(hs) => hs.add(h)
        case None => update(key, h)
      }
    }

    def set(key: String, value: String): Unit = {
      val h = new Header(key, value)
      update(key, h)
    }

    def removeAll(key: String): Unit = {
      remove(key)
    }

  }

  private final class Header(val name: String, val value: String, var next: Header = null) {

    def values: Seq[String] =
      if (next == null) value :: Nil
      else {
        val result = new mutable.ListBuffer[String] += value

        var i = next
        do {
          result += i.value
          i = i.next
        } while (i != null)

        result.toList
      }

    def names: Seq[String] =
      if (next == null) name :: Nil
      else {
        val result = new mutable.ListBuffer[String] += name

        var i = next
        do {
          result += i.name
          i = i.next
        } while (i != null)

        result.toList
      }

    def add(h: Header): Unit = {
      var i = this
      while (i.next != null) {
        i = i.next
      }

      i.next = h
    }

  }

}