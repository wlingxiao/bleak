package goa.annotation

import goa.{Route, Method => HttpMethod}

import scala.reflect._
import scala.reflect.runtime.currentMirror
import scala.reflect.runtime.universe._
import scala.tools.reflect.ToolBox

class AnnotationProcessor {

  def process[T <: AnyRef : TypeTag : ClassTag](target: T): Seq[Route] = {
    val path = symbolOf[T].annotations.find(_.tree.tpe <:< typeOf[Path]).map(x => eval[Path](x.tree)).head
    typeOf[T].members.collect { case m: MethodSymbol => m }.map { method =>
      method.annotations.filter(a => isRouteMethod(a.tree.tpe)).map { a =>
        eval[Any](a.tree) match {
          case get: GET =>
            HttpMethod.Get -> get.value
          case post: POST =>
            HttpMethod.Post -> post.value
          case _ => throw new IllegalArgumentException
        }
      }.map { x =>
        val (httpMethod, methodAnno) = x
        val params = extractParam(method)
        val javaMethod = target.getClass.getDeclaredMethods.filter(x => x.getName == method.name.toString).head
        Route(path.value + methodAnno, httpMethod, Some(target), method, params, Map(Symbol("JavaMethod") -> javaMethod))
      }
    }.filter(_.nonEmpty).map(_.head).toSeq
  }

  def extractParam(invoker: MethodSymbol): Seq[RouteParam] = {
    invoker.paramLists.head.map { x =>
      x.annotations.filter(a => a.tree.tpe <:< typeOf[PathParam] || a.tree.tpe <:< typeOf[QueryParam])
        .map(x => eval[Any](x.tree)).collectFirst {
        case path: PathParam =>
          val name = if (path.value != null) Some(path.value) else Some(x.name.toString)
          RouteParam(Some("PathParam"), name, x.info)
        case query: QueryParam =>
          val name = if (query.value != null) Some(query.value) else Some(x.name.toString)
          RouteParam(Some("QueryParam"), name, x.info)
      }.getOrElse(RouteParam(None, None, x.info))
    }
  }

  private def isRouteMethod(tpe: Type): Boolean = {
    tpe <:< typeOf[GET] || tpe <:< typeOf[POST]
  }

  private def eval[T](tree: Tree): T = {
    val tb = currentMirror.mkToolBox()
    tb.eval(tb.untypecheck(tree)).asInstanceOf[T]
  }
}