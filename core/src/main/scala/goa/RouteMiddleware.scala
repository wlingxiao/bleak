package goa

import goa.marshalling.MessageBodyWriter
import goa.matcher.PathMatcher

import goa.annotation._

private[goa] class RouteMiddleware(mapper: MessageBodyWriter, app: Application, pathMatcher: PathMatcher) extends Middleware {

  override def apply(ctx: Context): Unit = {
    val r = findMatchedRouter(request)
    if (r.isDefined) {
      runRouterAction(r.get, request, response, pathMatcher)
    }
  }

  private def findMatchedRouter(request: Request): Option[Route] = {
    val urlMatched = app.routers.filter(r => pathMatcher.tryMatch(r.path, request.path))
    if (urlMatched.isEmpty) {
      response.status = Status.NotFound
      return None
    }
    val methodMatched = urlMatched.filter(r => r.method == request.method)
    if (methodMatched.isEmpty) {
      response.status = Status.MethodNotAllowed
      return None
    }
    val finalMatched = methodMatched.sortWith((x, y) => {
      pathMatcher.getPatternComparator(request.uri).compare(x.path, y.path) > 0
    })
    finalMatched.headOption
  }

  private def runRouterAction(router: Route, request: Request, response: Response, pathMatcher: PathMatcher): Unit = {
    val requestWithRouter = new RequestWithRouterParam(request, router, pathMatcher)
    Goa.putMessage(requestWithRouter -> response)
    router.target match {
      case Some(target) =>
        target match {
          case _: Controller =>
            val any = router.action.asInstanceOf[() => Any]()
            response.body = mapper.write(response, any)
          case _ =>
            val paramMap = mapRouteParam(router, requestWithRouter)
            val action = router.action.asInstanceOf[java.lang.reflect.Method]
            response.body = mapper.write(response, action.invoke(target, paramMap.values.toSeq.map(x => x.asInstanceOf[Object]): _*))
        }
      case None =>

    }
  }

  private[goa] def mapRouteParam(router: Route, request: Request): Map[String, Any] = {
    router.params.map { x =>
      val param = x.param
      val parameterType = x.parameter.getType
      param match {
        case Some(p) =>
          parameterType match {
            case m if m.isAssignableFrom(classOf[Long]) =>
              var paramName: String = p match {
                case pathParam: path => pathParam.value()
                case queryParam: query => queryParam.value()
                case _ => throw new IllegalStateException()
              }
              paramName = if (paramName == "") x.parameter.getName else paramName
              paramName -> request.params.get(paramName).getOrElse("0").toLong
            case m if m.isAssignableFrom(classOf[String]) =>
              var paramName: String = p match {
                case pathParam: path => pathParam.value()
                case queryParam: query => queryParam.value()
                case _ => throw new IllegalStateException()
              }
              paramName = if (paramName == "") x.parameter.getName else paramName
              paramName -> request.params.get(paramName).getOrElse("0").toLong
            case _ =>
              var paramName: String = p match {
                case pathParam: path => pathParam.value()
                case queryParam: query => queryParam.value()
                case _ => throw new IllegalStateException()
              }
              paramName = if (paramName == "") x.parameter.getName else paramName
              paramName -> fromMap(request.params.flat(), parameterType)
          }
        case None =>
          parameterType match {
            case m if m.isAssignableFrom(classOf[Long]) =>
              val paramName: String = x.parameter.getName
              paramName -> request.params.get(paramName).getOrElse("0").toLong
            case m if m.isAssignableFrom(classOf[String]) =>
              val paramName = x.parameter.getName
              paramName -> request.params.get(paramName).getOrElse("0")
            case _ =>
              val paramName = x.parameter.getName
              paramName -> fromMap(request.params.flat(), parameterType)
          }
      }

    }.toMap
  }

  private def fromMap(m: Map[String, String], info: Class[_]): Any = {
    val constructor = info.getConstructors.head
    val constructorArgs: Seq[Object] = constructor.getParameters.map { param =>
      val paramName = param.getName
      if (param.getType.isAssignableFrom(classOf[Option[_]])) {
        m.get(paramName)
      } else {
        m.get(paramName).map { x =>
          if (param.getType.isAssignableFrom(classOf[Long])) {
            x.toLong.longValue()
          } else if (param.getType.isAssignableFrom(classOf[String])) {
            x
          }
        }.getOrElse(throw new IllegalArgumentException("Map is missing required parameter named " + paramName))
      }
    }.map(x => x.asInstanceOf[Object])
    constructor.newInstance(constructorArgs: _*)
  }
}
