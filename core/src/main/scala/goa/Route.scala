package goa

case class Route(path: String,
                 method: String,
                 controller: Controller,
                 action: () => Any)
