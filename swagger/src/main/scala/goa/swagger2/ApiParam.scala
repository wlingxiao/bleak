package goa.swagger2

import scala.reflect._

trait ApiParam {

  def name: String

  def desc: String

  def required: Boolean

}

case class PathParam[T: ClassTag](name: String,
                                  desc: String = "",
                                  required: Boolean = false,
                                  readOnly: Boolean = false) extends ApiParam {

  def tpe: ClassTag[T] = classTag[T]

}

case class QueryParam[T: ClassTag](name: String,
                                   desc: String = "",
                                   required: Boolean = false,
                                   readOnly: Boolean = false) extends ApiParam {

  def tpe: ClassTag[T] = classTag[T]
}

case class BodyParam[T: ClassTag](name: String = "",
                                  desc: String = "",
                                  required: Boolean = false,
                                  readOnly: Boolean = false) extends ApiParam {
  def tpe: ClassTag[T] = classTag[T]
}
