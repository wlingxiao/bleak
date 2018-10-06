package goa.swagger2

import scala.reflect.runtime.universe._

case class ApiOperation[T: TypeTag](value: String,
                                    notes: String = "",
                                    hidden: Boolean = false,
                                    nickname: String = "",
                                    responseHeaders: Seq[ResponseHeader] = Nil,
                                    httpMethod: String = "",
                                    authorizations: Seq[Authorization] = Nil,
                                    consumes: Seq[String] = Nil,
                                    produces: Seq[String] = Nil,
                                    responseReference: String = "",
                                    code: Int = 200,
                                    deprecated: Boolean = false,
                                    tags: Seq[String] = Nil) {

  def tpe: TypeTag[T] = typeTag[T]

}
