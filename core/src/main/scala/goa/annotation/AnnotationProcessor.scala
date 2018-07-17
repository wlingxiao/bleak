package goa.annotation

import goa.{Route, Method => HttpMethod}

import scala.reflect._
import scala.reflect.runtime.universe._

class AnnotationProcessor {
  private val Params = Array(typeOf[PathParam], typeOf[QueryParam], typeOf[Body], typeOf[cookie], typeOf[header])

  def process[T <: AnyRef : TypeTag : ClassTag](target: T): Seq[Route] = {
    val path = symbolOf[T].annotations.find(isPathAnnotation).map(extractPath).head
    typeOf[T].members.collect { case m: MethodSymbol => m }.map { method =>
      method.annotations.filter(a => isRouteMethod(a.tree.tpe)).map { a =>
        extractRoute(a) match {
          case get: GET =>
            HttpMethod.Get -> get.value
          case post: POST =>
            HttpMethod.Post -> post.value
          case _ => throw new IllegalArgumentException
        }
      }.map { x =>
        val (httpMethod, value) = x
        val params = extractParam(method)
        Route(path.value + value, httpMethod, Some(target), method, params)
      }
    }.filter(_.nonEmpty).map(_.head).toSeq
  }

  private def extractParam(invoker: MethodSymbol): Seq[RouteParam] = {
    invoker.paramLists.head.map { param =>
      param.annotations.filter(isParam).map(extractParam).collectFirst {
        case path: PathParam =>
          val name = Option(path.value).getOrElse(param.name.toString)
          RouteParam(Option(PathParam(name, path.required)), param)
        case query: QueryParam =>
          val name = Option(query.value).getOrElse(param.name.toString)
          RouteParam(Option(QueryParam(name, query.required)), param)
        case body: Body =>
          val name = Option(body.value).getOrElse(param.name.toString)
          RouteParam(Option(Body(name, body.required)), param)
        case c: cookie =>
          val name = Option(c.value).getOrElse(param.name.toString)
          RouteParam(Option(cookie(name, c.required)), param)
        case h: header =>
          val name = Option(h.value).getOrElse(param.name.toString)
          RouteParam(Option(header(name, h.required)), param)
      }.getOrElse {
        RouteParam(Some(QueryParam(param.name.toString)), param)
      }
    }
  }

  private def isPathAnnotation(anno: Annotation): Boolean = {
    anno.tree.tpe <:< typeOf[Path]
  }

  private def extractPath(anno: Annotation): Path = {
    anno.tree.children.tail match {
      case List(Literal(Constant(value: String))) =>
        Path(value)
      case List(Select(_, _)) =>
        Path()
    }
  }

  private def extractRoute(anno: Annotation): Any = {
    anno.tree.children.tail match {
      case List(Literal(Constant(value: String))) =>
        if (anno.tree.tpe <:< typeOf[GET]) GET(value)
        else if (anno.tree.tpe <:< typeOf[POST]) POST(value)
        else throw new IllegalStateException()
      case List(Select(_, _)) =>
        if (anno.tree.tpe <:< typeOf[GET]) GET()
        else if (anno.tree.tpe <:< typeOf[POST]) POST()
        else throw new IllegalStateException()
    }
  }

  private def isParam(anno: Annotation): Boolean = {
    Params.exists(x => anno.tree.tpe <:< x)
  }

  def extractParam(anno: Annotation, value: String, required: Boolean): Any = {
    if (anno.tree.tpe <:< typeOf[PathParam]) PathParam(value, required)
    else if (anno.tree.tpe <:< typeOf[QueryParam]) QueryParam(value, required)
    else if (anno.tree.tpe <:< typeOf[Body]) Body(value, required)
    else if (anno.tree.tpe <:< typeOf[header]) header(value, required)
    else if (anno.tree.tpe <:< typeOf[cookie]) cookie(value, required)
    else throw new IllegalStateException()
  }

  private def extractParam(anno: Annotation): Any = {
    anno.tree.children.tail match {
      case List(Literal(Constant(value: String)), Literal(Constant(required: Boolean))) =>
        extractParam(anno, value, required)
      case List(Select(_, _), Literal(Constant(required: Boolean))) =>
        extractParam(anno, null, required)
      case List(Literal(Constant(value: String)), Select(_, _)) =>
        extractParam(anno, value, required = false)
      case List(Select(_, _), Select(_, _)) =>
        extractParam(anno, null, required = false)
    }
  }


  private def isRouteMethod(tpe: Type): Boolean = {
    tpe <:< typeOf[GET] || tpe <:< typeOf[POST]
  }
}