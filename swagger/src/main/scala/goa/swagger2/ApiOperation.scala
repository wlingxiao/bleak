package goa.swagger2

case class ApiOperation(value: String,
                        notes: String = "",
                        hidden: Boolean = false,
                        nickname: String = "",
                        responseHeaders: Seq[ResponseHeader] = Nil,
                        httpMethod: Seq[String] = Nil)
