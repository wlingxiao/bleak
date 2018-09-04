package goa

import java.util.concurrent.ConcurrentHashMap

abstract class Router {

  def path: String

  def methods: Seq[Method]

  def action: Action

  def apply(ac: Action): Router

  def name: String

  def name(name: String): Router

  def attr(key: Symbol): Option[Any]

  def attr(key: Symbol, value: Any): Router

}

object Router {

  class Impl(val path: String, val methods: Seq[Method]) extends Router {

    private var _action: Action = _

    private var _name: String = _

    private val attributes = new ConcurrentHashMap[Symbol, Any]()

    override def action: Action = _action

    override def apply(action: Action): Router = {
      this._action = action
      this
    }

    override def name: String = _name

    override def name(name: String): Router = {
      _name = name
      this
    }

    override def attr(key: Symbol): Option[Any] = {
      Option(attributes.get(key))
    }

    override def attr(key: Symbol, value: Any): Router = {
      attributes.put(key, value)
      this
    }
  }

  def apply(path: String, methods: Seq[Method]): Router = new Impl(path, methods)

}
