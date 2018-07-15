package goa.annotation

import scala.annotation.StaticAnnotation

case class Body(value: String = null) extends StaticAnnotation
