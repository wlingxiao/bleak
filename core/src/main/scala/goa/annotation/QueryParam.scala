package goa.annotation

import scala.annotation.StaticAnnotation

case class QueryParam(value: String = null, required: Boolean = false) extends StaticAnnotation
