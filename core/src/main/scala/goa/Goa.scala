package goa

import goa.matcher.{AntPathMatcher, PathMatcher}

trait Goa extends App with Server {

  val pathMatcher: PathMatcher = new AntPathMatcher()

}