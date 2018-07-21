package goa.annotation

import java.lang.reflect.Method

import goa.{Route, Method => HttpMethod}

import scala.reflect._

class AnnotationProcessor {

  def process[T: ClassTag](target: T): Seq[Route] = {
    val clazz = target.getClass
    val path = clazz.getAnnotation(classOf[route])
    val ret = clazz.getMethods.map { method =>
      method.getAnnotations.collect {
        case getRoute: get => HttpMethod.Get -> getRoute.value()
        case postRoute: post => HttpMethod.Post -> postRoute.value()
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
        case pathParam: path =>
          RouteParam(Option(pathParam), param)
        case queryParam: query =>
          RouteParam(Option(queryParam), param)
        case requestBody: body =>
          RouteParam(Option(requestBody), param)
        case cookieParam: cookie =>
          RouteParam(Option(cookieParam), param)
        case headerParam: header =>
          RouteParam(Option(headerParam), param)
      }.getOrElse {
        RouteParam(None, param)
      }
    }
  }
}