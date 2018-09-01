package goa.swagger2

case class ApiOperation(value: String,
                        notes: String = "",
                        hidden: Boolean = false,
                        nickname: String = "",
                        responseHeaders: Seq[ResponseHeader] = Nil,
                        response: Class[_] = null,
                        responseContainer: String = "",
                        httpMethod: String = "",
                        authorizations: Seq[Authorization] = Nil,
                        consumes: Seq[String] = Nil,
                        produces: Seq[String] = Nil,
                        responseReference: String = "",
                        code: Int = 200,
                        deprecated: Boolean = false,
                        tags: Seq[String] = Nil)
