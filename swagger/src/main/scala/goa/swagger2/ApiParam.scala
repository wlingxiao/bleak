package goa.swagger2

trait ApiParam {

  def name: String

}

case class PathParam(name: String) extends ApiParam
