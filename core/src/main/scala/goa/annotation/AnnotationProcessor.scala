package goa.annotation

import goa.{Route, Method => HttpMethod}

import scala.collection.mutable.ListBuffer
import scala.reflect._
import scala.reflect.runtime.universe._

class AnnotationProcessor {

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
    val ret = invoker.paramLists.head.map { param =>
      param.annotations.filter(isParam).map(extractParam).collectFirst {
        case path: PathParam =>
          val name = Option(path.value).getOrElse(param.name.toString)
          RouteParam(Option(PathParam(name, path.required)), param)
        case query: QueryParam =>
          val name = Option(query.value).getOrElse(param.name.toString)
          RouteParam(Option(QueryParam(name, query.required)), param)
      }.getOrElse {
        if (isPrimaryType(param.info)) {
          RouteParam(Some(QueryParam(param.name.toString)), param)
        } else {
          param.info.decls.collectFirst { case m: MethodSymbol if m.isConstructor => m }.map { a =>
            a.paramLists.head.map(x => RouteParam(Some(QueryParam(x.name.toString)), x))
          }.head
        }
      }
    }
    val buf = ListBuffer[RouteParam]()
    ret.foreach {
      case o: RouteParam =>
        buf += o
      case seq: Seq[_] =>
        buf ++= seq.asInstanceOf[Seq[RouteParam]]
    }
    buf
  }

  private def isPrimaryType(info: Type): Boolean = {
    info <:< typeOf[Long] || info <:< typeOf[String]
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
    anno.tree.tpe <:< typeOf[PathParam] || anno.tree.tpe <:< typeOf[QueryParam]
  }

  private def extractParam(anno: Annotation): Any = {
    anno.tree.children.tail match {
      case List(Literal(Constant(value: String)), Literal(Constant(required: Boolean))) =>
        if (anno.tree.tpe <:< typeOf[PathParam]) PathParam(value, required)
        else if (anno.tree.tpe <:< typeOf[QueryParam]) QueryParam(value, required)
        else throw new IllegalStateException()
      case List(Select(_, _), Literal(Constant(required: Boolean))) =>
        if (anno.tree.tpe <:< typeOf[PathParam]) PathParam(required = required)
        else if (anno.tree.tpe <:< typeOf[QueryParam]) QueryParam(required = required)
        else throw new IllegalStateException()
      case List(Literal(Constant(value: String)), Select(_, _)) =>
        if (anno.tree.tpe <:< typeOf[PathParam]) PathParam(value = value)
        else if (anno.tree.tpe <:< typeOf[QueryParam]) QueryParam(value = value)
        else throw new IllegalStateException()
      case List(Select(_, _), Select(_, _)) =>
        if (anno.tree.tpe <:< typeOf[PathParam]) PathParam()
        else if (anno.tree.tpe <:< typeOf[QueryParam]) QueryParam()
        else throw new IllegalStateException()
    }
  }

  private def except(m: MethodSymbol): Boolean = {
    m.isConstructor
    //!m.isPrivate && !m.isConstructor && !typeOf[Any].members.exists(mem => mem.info =:= m.info) && !m.name.toString.contains("copy") && !m.name.toString.contains("product")
  }

  private def isRouteMethod(tpe: Type): Boolean = {
    tpe <:< typeOf[GET] || tpe <:< typeOf[POST]
  }
}