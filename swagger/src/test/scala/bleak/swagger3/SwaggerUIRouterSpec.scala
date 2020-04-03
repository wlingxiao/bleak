package bleak.swagger3

import org.specs2.mutable._

import scala.util.{Failure, Success}

class SwaggerUIRouterSpec extends Specification {

  "SwaggerUIRouter.loadFileInfo" should {
    "load file info" in {
      val swaggerUIRouter = new SwaggerUIRouter
      val fi = swaggerUIRouter.loadFileInfo("index.html")
      fi match {
        case Failure(exception) => 1 should_== 2
        case Success(value) => value.filename should_== "index.html"
      }
    }
  }

}
