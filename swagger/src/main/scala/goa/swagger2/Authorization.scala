package goa.swagger2

case class Authorization(value: String, scopes: Seq[AuthorizationScope] = Nil)
