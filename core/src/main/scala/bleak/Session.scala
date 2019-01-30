package bleak

import bleak.util.AttributeMap

/**
  * Represents a HTTP session
  *
  */
trait Session extends AttributeMap {

  def id: String

  def creationTime: Long

  def lastAccessedTime: Long

  def invalidate(): Unit

}

/**
  * Listener for session events
  */
trait SessionListener {

  /**
    * Called when a session is created
    */
  def created(session: Session): Unit = {}

  /**
    * Called when a session is destroyed
    */
  def destroyed(session: Session): Unit = {}

}

trait SessionManager {

  /**
    * Creates a new [[Session]]
    */
  def createSession(request: Request): Session

  /**
    * Retrieves a [[Session]] with the given session id
    */
  def session(sessionId: String): Option[Session]

  def session(ctx: Context): Option[Session]

  def registerSessionListener(listener: SessionListener): Unit

  def removeSessionListener(listener: SessionListener): Unit

}
