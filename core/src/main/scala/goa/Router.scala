package goa

case class Router(path: String,
                  method: String,
                  controller: Controller,
                  action: () => Any)
