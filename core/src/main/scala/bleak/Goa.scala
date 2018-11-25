package bleak

import bleak.matcher.{AntPathMatcher, PathMatcher}

trait Goa extends App with Server {

  val pathMatcher: PathMatcher = new AntPathMatcher()

}