package goa

case class Route(path: String,
                 method: Method,
                 controller: Controller,
                 action: () => Any,
                 meta: Map[Symbol, Any] = Map.empty) {
}