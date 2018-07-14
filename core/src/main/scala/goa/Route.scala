package goa

import goa.annotation.RouteParam

case class Route(path: String,
                 method: Method,
                 target: Option[Any],
                 action: Any,
                 params: Seq[RouteParam] = Nil,
                 meta: Map[Symbol, Any] = Map.empty) {
}