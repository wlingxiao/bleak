package bleak
package swagger3

/**
  * Alias for [[io.swagger.v3.oas.models.servers.Server]]
  */
case class Server(url: String,
                  desc: String = "",
                  vars: Map[String, ServerVar] = Map.empty,
                  extensions: Map[String, AnyRef] = Map.empty)

/**
  * Alias for [[io.swagger.v3.oas.models.servers.ServerVariable]]
  */
case class ServerVar(enum: Iterable[String] = Nil,
                     default: String = "",
                     desc: String = "",
                     extensions: Map[String, AnyRef] = Map.empty)