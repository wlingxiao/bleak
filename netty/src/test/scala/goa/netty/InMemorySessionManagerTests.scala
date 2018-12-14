package goa.netty

import goa._
import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite, Matchers}

class InMemorySessionManagerTests extends FunSuite with Matchers with BeforeAndAfter with MockitoSugar {

  import InMemorySessionManager._

  var mockRequest: Request = _
  var inMemorySessionManager: InMemorySessionManager = _
  var mockCookies: Cookies = _
  var mockSession: Session = _

  before {
    mockRequest = mock[Request]
    inMemorySessionManager = new InMemorySessionManager
    mockCookies = mock[Cookies]
    mockSession = mock[Session]
  }

  ignore("create session when current request has cookie for session") {
    when(mockCookies.get(DefaultSessionId)).thenReturn(Option(Cookie("a", "123")))
    when(mockRequest.cookies).thenReturn(mockCookies)

    var i = 1
    inMemorySessionManager.registerSessionListener(new SessionListener {
      override def created(session: Session): Unit = {
        i += 1
      }
    })

    val session = inMemorySessionManager.createSession(mockRequest)
    session.id shouldEqual "123"
    i shouldEqual 2
  }

  ignore("create session when cookie for session non exists") {
    when(mockCookies.get(DefaultSessionId)).thenReturn(None)
    when(mockRequest.cookies).thenReturn(mockCookies)

    var i = 1
    inMemorySessionManager.registerSessionListener(new SessionListener {
      override def created(session: Session): Unit = {
        i += 1
      }
    })

    val session = inMemorySessionManager.createSession(mockRequest)
    session.id shouldNot be(null)
    i shouldEqual 2
  }

  ignore("invalidate session") {
    when(mockCookies.get(DefaultSessionId)).thenReturn(None)
    when(mockRequest.cookies).thenReturn(mockCookies)

    var i = 1
    inMemorySessionManager.registerSessionListener(new SessionListener {
      override def destroyed(session: Session): Unit = {
        i += 1
      }
    })

    val session = inMemorySessionManager.createSession(mockRequest)
    session.invalidate()
    i shouldEqual 2
  }

}
