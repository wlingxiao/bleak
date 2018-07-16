package goa.annotation

import scala.annotation.StaticAnnotation

case class Body(value: String = null, required: Boolean = false) extends StaticAnnotation
