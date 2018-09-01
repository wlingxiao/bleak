package goa.swagger2

case class Api(tags: Seq[String] = Nil,
               produces: String = "",
               consumes: String = "",
               protocols: String = "",
               hidden: Boolean = false)
