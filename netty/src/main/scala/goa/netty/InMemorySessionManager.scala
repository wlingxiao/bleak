package goa
package netty

import java.util
import java.util.concurrent.{ConcurrentHashMap, CopyOnWriteArrayList}

import logging.Logging

class InMemorySessionManager extends SessionManager with Logging {

  import InMemorySessionManager._

  private val sessions = new ConcurrentHashMap[String, DefaultSession]()

  private[this] val sessionIdGenerator = new UUIDSessionIdGenerator

  private[this] val sessionListeners = new CopyOnWriteArrayList[SessionListener]()

  override def createSession(request: Request): Session = {
    val sessionId = findSessionIdFromRequest(request).getOrElse(sessionIdGenerator.createSessionId())
    val session = new DefaultSession(this, sessionId)
    sessions.put(sessionId, session)
    sessionCreated(session)
    session
  }

  override def session(sessionId: String): Option[Session] = {
    if (sessionId == null) None
    else Option(sessions.get(sessionId))
  }

  override def session(request: Request): Option[Session] = {
    findSessionIdFromRequest(request).flatMap(session)
  }

  private def findSessionIdFromRequest(request: Request): Option[String] = {
    request.cookies.get(DefaultSessionId).flatMap { cookie =>
      log.trace(s"Found cookie for session id ${cookie.value}")
      cookie.value
    }
  }

  private def sessionCreated(session: Session): Unit = {
    sessionListeners.forEach { listener =>
      listener.created(session)
    }
  }

  private def sessionDestroyed(session: Session): Unit = {
    val listeners = new util.ArrayList(sessionListeners)
    val iterator = listeners.listIterator(listeners.size())
    while (iterator.hasPrevious) {
      iterator.previous().destroyed(session)
    }
  }

  override def registerSessionListener(listener: SessionListener): Unit = {
    sessionListeners.add(listener)
  }

  override def removeSessionListener(listener: SessionListener): Unit = {
    sessionListeners.remove(listener)
  }
}

object InMemorySessionManager {

  val DefaultSessionId = "JSESSIONID"

  class DefaultSession(sessionManager: InMemorySessionManager, val id: String) extends Session {

    @volatile
    private[this] var invalid = false

    @volatile
    private[this] var accessedTime = System.currentTimeMillis()

    override val creationTime: Long = System.currentTimeMillis()

    override def lastAccessedTime: Long = accessedTime

    override def invalidate(): Unit = {
      this.synchronized {
        val session = sessionManager.sessions.remove(id)
        if (session == null) {
          return
        }
      }
      sessionManager.sessionDestroyed(this)
      invalid = true
    }
  }

}
