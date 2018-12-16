package bleak

import bleak.matcher.{AntPathMatcher, PathMatcher}

trait Goa extends Application with Server {

  val pathMatcher: PathMatcher = new AntPathMatcher()

}