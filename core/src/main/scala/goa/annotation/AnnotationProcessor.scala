package goa.annotation

import java.lang.reflect.Method

import goa.annotation.internal._
import goa.{Route, Method => HttpMethod}

import scala.reflect._

class AnnotationProcessor {

  def process[T: ClassTag](target: T): Seq[Route] = {
    val clazz = target.getClass
    val path = clazz.getAnnotation(classOf[route])
    val ret = clazz.getMethods.map { method =>
      method.getAnnotations.collect {
        case getRoute: GetRoute => HttpMethod.Get -> getRoute.value()
        case postRoute: PostRoute => HttpMethod.Post -> postRoute.value()
      }.map { x =>
        val (httpMethod, value) = x
        Route(path.value + value, httpMethod, Some(target), method, extractParam(method))
      }
    }
    ret.filter(_.nonEmpty).map(x => x.head)
  }

  private def extractParam(method: Method): Seq[RouteParam] = {
    method.getParameters.map { param =>
      param.getAnnotations.collectFirst {
        case pathParam: PathParam =>
          RouteParam(Option(pathParam), param)
        case queryParam: QueryParam =>
          RouteParam(Option(queryParam), param)
        case requestBody: RequestBody =>
          RouteParam(Option(requestBody), param)
        case cookieParam: CookieParam =>
          RouteParam(Option(cookieParam), param)
        case headerParam: HeaderParam =>
          RouteParam(Option(headerParam), param)
      }.getOrElse {
        RouteParam(None, param)
      }
    }
  }
}