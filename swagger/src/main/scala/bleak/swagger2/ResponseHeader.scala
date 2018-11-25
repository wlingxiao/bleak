package bleak.swagger2

case class ResponseHeader(name: String = "",
                          description: String = "",
                          response: Class[_] = classOf[Unit],
                          responseContainer: String = "") {}
