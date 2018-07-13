package goa.annotation

import scala.annotation.StaticAnnotation

case class Path(value: String = "") extends StaticAnnotation
