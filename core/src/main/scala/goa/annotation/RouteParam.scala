package goa.annotation

import java.lang.reflect.Parameter

private[goa] case class RouteParam(param: Option[Any], parameter: Parameter)
