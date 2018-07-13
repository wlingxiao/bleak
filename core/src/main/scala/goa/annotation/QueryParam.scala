package goa.annotation

import scala.annotation.StaticAnnotation

case class QueryParam(value: String = null) extends StaticAnnotation
