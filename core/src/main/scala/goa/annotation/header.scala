package goa.annotation

import scala.annotation.StaticAnnotation

case class header(value: String = null, required: Boolean = false) extends StaticAnnotation

