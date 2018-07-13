package goa.annotation

import scala.annotation.StaticAnnotation

case class POST(value: String = "") extends StaticAnnotation

