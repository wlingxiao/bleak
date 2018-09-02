package goa

import goa.matcher.{AntPathMatcher, PathMatcher}

abstract class Goa extends App {
  self: Server =>

  val pathMatcher: PathMatcher = new AntPathMatcher()

  val routerMiddleware = new RouteMiddleware(this, pathMatcher)

  private def doStart(): Unit = {
    use(routerMiddleware)
    initModules()
    start()
  }

  def run(): Unit = {
    doStart()
  }

  def stop(): Unit = {
    clearRoutes()
    destroyModules()
    close()
  }
}