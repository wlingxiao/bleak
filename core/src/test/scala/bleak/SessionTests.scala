package bleak

import org.mockito.Mockito._
import org.scalatest.mockito.MockitoSugar

class SessionTests extends BaseTests with MockitoSugar {

  /*import SessionManager._

  var mockRequest: Request = _
  var mockSessionManager: SessionManager = _
  var mockCookies: Cookies = _
  var mockSession: Session = _
  before {
    mockRequest = mock[Request]
    mockSessionManager = mock[SessionManager]
    mockCookies = mock[Cookies]
    mockSession = mock[Session]
  }

  test("test session exists") {
    when(mockCookies.get(SessionIdKey)).thenReturn(Option(Cookie("a", "123")))
    when(mockRequest.cookies).thenReturn(mockCookies)
    when(mockSessionManager.get("123")).thenReturn(mockSession)

    val sessionRequest = new SessionRequest(mockRequest, mockSessionManager)
    sessionRequest.session shouldEqual mockSession

  }

  test("test session does not exists should create new session") {
    when(mockCookies.get(SessionIdKey)).thenReturn(Option(Cookie("a", "123")))
    when(mockRequest.cookies).thenReturn(mockCookies)
    when(mockSessionManager.createSession()).thenReturn(mockSession)

    val sessionRequest = new SessionRequest(mockRequest, mockSessionManager)
    sessionRequest.session shouldEqual mockSession
    sessionRequest.attr[String](CreateSessionKey).get shouldEqual Some(mockSession)
  }

  test("test session does not exists") {
    when(mockCookies.get(SessionIdKey)).thenReturn(None)
    when(mockRequest.cookies).thenReturn(mockCookies)
    when(mockSessionManager.createSession()).thenReturn(mockSession)

    val sessionRequest = new SessionRequest(mockRequest, mockSessionManager)
    sessionRequest.session shouldEqual mockSession
    sessionRequest.attr[String](CreateSessionKey).get shouldEqual Some(mockSession)
  }*/

}
