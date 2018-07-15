package goa.annotation

import scala.reflect.runtime.universe._

private[goa] case class RouteParam(param: Option[Any], symbol: Symbol)
