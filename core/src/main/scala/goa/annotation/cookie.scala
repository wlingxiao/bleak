package goa.annotation

import scala.annotation.StaticAnnotation

case class cookie(value: String = null, required: Boolean = false) extends StaticAnnotation
