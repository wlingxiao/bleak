package goa.annotation

import scala.annotation.StaticAnnotation

case class PathParam(value: String = null, required: Boolean = false) extends StaticAnnotation