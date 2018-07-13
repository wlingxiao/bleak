package goa.annotation

import scala.annotation.StaticAnnotation

case class PathParam(value: String = null) extends StaticAnnotation