package goa

case class Router(path: String,
                  method: String,
                  action: () => Any)
