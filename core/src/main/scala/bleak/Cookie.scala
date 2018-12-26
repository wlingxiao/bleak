package bleak


case class Cookie(name: String,
                  value: String,
                  domain: String = "",
                  path: String = "",
                  maxAge: Long = Long.MinValue,
                  secure: Boolean = false,
                  httpOnly: Boolean = false)