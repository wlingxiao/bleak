package goa.netty

import java.util.{Base64, UUID}

class UUIDSessionIdGenerator {

  def createSessionId(): String = {
    val uuid = UUID.randomUUID().toString
    Base64.getEncoder.encodeToString(uuid.getBytes())
  }

}
