package goa

import goa.annotation.{PathParam, QueryParam}
import goa.marshalling.MessageBodyWriter
import goa.matcher.PathMatcher

import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._

private class RouteMiddleware(mapper: MessageBodyWriter, app: Application, pathMatcher: PathMatcher) extends Middleware {

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

  private def reflect(target: Any): InstanceMirror = {
    currentMirror.reflect(target)
  }

  private def runRouterAction(router: Route, request: Request, response: Response, pathMatcher: PathMatcher): Unit = {
    val requestWithRouter = new RequestWithRouterParam(request, router, pathMatcher)
    Goa.putMessage(requestWithRouter -> response)
    router.target match {
      case Some(t) =>
        t match {
          case _: Controller =>
            val any = router.action.asInstanceOf[() => Any]()
            response.body = mapper.write(response, any)
          case _ =>
            val paramMap = mapRouteParam(router, requestWithRouter)
            val target = reflect(router.target.get)
            val action = router.action.asInstanceOf[MethodSymbol]
            response.body = mapper.write(response, target.reflectMethod(action)(paramMap.values.toSeq: _*))
        }
      case None =>

    }
  }

  private def mapRouteParam(router: Route, request: Request): Map[String, Any] = {
    router.params.map(x => {
      val param = x.param
      val info = x.symbol.info
      param match {
        case Some(p) =>
          info match {
            case m if m <:< typeOf[Long] =>
              val paramName = p match {
                case p: PathParam => p.value
                case q: QueryParam => q.value
                case _ => throw new IllegalStateException()
              }
              paramName -> request.params.get(paramName).getOrElse("0").toLong
            case m if m <:< typeOf[String] =>
              val paramName = p match {
                case p: PathParam => p.value
                case q: QueryParam => q.value
                case _ => throw new IllegalStateException()
              }
              paramName -> request.params.get(paramName).getOrElse("")
            case _ =>
              val paramName = p match {
                case p: PathParam => p.value
                case q: QueryParam => q.value
                case _ => throw new IllegalStateException()
              }
              paramName -> fromMap(request.params.flat(), info)
          }
        case None =>
          info match {
            case m if m <:< typeOf[Long] => x.symbol.name.toString -> 0L
            case m if m <:< typeOf[String] => x.symbol.name.toString -> ""
            case _ => throw new IllegalStateException()
          }
      }
    }).toMap
  }

  private def fromMap[T: TypeTag](m: Map[String, _], info: Type): Any = {
    val classTest = info.typeSymbol.asClass
    val classMirror = currentMirror.reflectClass(classTest)
    val constructor = info.decl(termNames.CONSTRUCTOR).asMethod
    val constructorMirror = classMirror.reflectConstructor(constructor)
    val constructorArgs = constructor.paramLists.flatten.map((param: Symbol) => {
      val paramName = param.name.toString
      if (param.typeSignature <:< typeOf[Option[Any]])
        m.get(paramName)
      else
        m.getOrElse(paramName, throw new IllegalArgumentException("Map is missing required parameter named " + paramName))
    })
    constructorMirror(constructorArgs: _*)
  }

}
