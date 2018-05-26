package goa

case class Cookie(name: String,
                  value: String,
                  domain: String = null,
                  path: String = "/",
                  maxAge: Long = 0,
                  secure: Boolean = true,
                  httpOnly: Boolean = false)
