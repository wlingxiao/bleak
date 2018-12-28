package bleak


import scala.collection.mutable

abstract class Cookies extends mutable.Map[String, Cookie] {

  def getAll(key: String): Seq[Cookie]

  def getOrNull(key: String): Cookie = get(key).orNull

  def add(cookie: Cookie): Cookies

}



