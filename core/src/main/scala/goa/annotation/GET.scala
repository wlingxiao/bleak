package goa.annotation

import scala.annotation.StaticAnnotation

case class GET(value: String = "") extends StaticAnnotation
