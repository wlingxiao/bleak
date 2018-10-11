package goa

import java.util.concurrent.ConcurrentHashMap

import goa.util.{AttributeMap, Executions}

import scala.concurrent.{ExecutionContext, Future}

trait Session extends AttributeMap {

  def id: String

}

class SessionMiddleware(sessionManager: SessionManager) extends Middleware {

  import SessionManager._

  protected implicit val ec: ExecutionContext = Executions.directec

  override def apply(ctx: Context): Future[Response] = {
    val request = ctx.request
    val sessionRequest = new SessionRequest(request, sessionManager)
    ctx.request(sessionRequest)
    ctx.next().map { response =>
      ctx.request.attr[Session](CreateSessionKey).get match {
        case Some(value) =>
          response.cookies.add(Cookie(SessionIdKey, value.id))
        case None =>
      }
      response
    }
  }

}

object SessionMiddleware {

  val sessionManager = new SessionManager

  def apply(sessionManager: SessionManager = sessionManager): SessionMiddleware = {
    new SessionMiddleware(sessionManager)
  }

}

class SessionManager {

  protected var sessions = new ConcurrentHashMap[String, Session]

  import java.util.{Base64, UUID}

  import SessionManager._

  def createSession(): Session = {
    val session = new DefaultSession(generateSessionId())
    sessions.put(session.id, session)
    session
  }

  def get(sessionId: String): Session = {
    sessions.get(sessionId)
  }

  protected def generateSessionId(): String = {
    val uuid = UUID.randomUUID().toString
    Base64.getEncoder.encodeToString(uuid.getBytes())
  }

}

object SessionManager {

  val SessionIdKey = "GSESSIONID"

  val CreateSessionKey = "CreateSession"

  class SessionRequest(val request: Request, manager: SessionManager) extends RequestProxy {

    override def session: Session = {
      session(true)
    }

    override def session(create: Boolean): Session = {
      cookies.get(SessionIdKey) match {
        case Some(cookie) =>
          val session = manager.get(cookie.value.get)
          if (!create) {
            return session
          }
          if (session != null) session else {
            val session = manager.createSession()
            attr(CreateSessionKey).set(session)
            session
          }
        case None =>
          val session = manager.createSession()
          attr(CreateSessionKey).set(session)
          session
      }
    }

  }

  def apply(): SessionManager = new SessionManager()

  class DefaultSession(val id: String) extends Session

}