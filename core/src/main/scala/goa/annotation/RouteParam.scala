package goa.annotation

import scala.reflect.runtime.universe._

case class RouteParam(paramType: Option[String], name: Option[String], info: Type)
