package bleak
package swagger3

import java.util.{ArrayList => JArrayList, List => JList}

import io.swagger.v3.oas.models.info.{Info => SInfo}
import io.swagger.v3.oas.models.servers.{ServerVariable, ServerVariables, Server => SServer}
import io.swagger.v3.oas.models.tags.{Tag => STag}
import io.swagger.v3.oas.models.{Components, OpenAPI, Paths}

import scala.collection.JavaConverters._
import scala.collection.mutable.ArrayBuffer

class Api {

  private val pathItemBuilders = new ArrayBuffer[PathItem]()

  def doc(routeName: String): PathItem = {
    val method = routeName.split(" ")(0).toLowerCase()
    val path = routeName.split(" ")(1)
    doc(Method(method), path)
  }

  private def doc(method: Method, path: String): PathItem = {
    val pathItemBuilder = PathItem(method, path)
    pathItemBuilders += pathItemBuilder
    pathItemBuilder
  }

  def build(config: Config): OpenAPI = {
    val openAPI = new OpenAPI
    if (config != null) {
      openAPI.setInfo(apiInfo(config.info))
      openAPI.setTags(apiTags(config.tags))
      openAPI.setServers(apiServers(config))
    }
    val paths = new Paths
    openAPI.setPaths(paths)
    val components = new Components
    openAPI.setComponents(components)
    for (p <- pathItemBuilders) {
      paths.addPathItem(p.path, p.build(openAPI))
    }
    openAPI.setPaths(paths)
    openAPI.setComponents(components)
    openAPI
  }

  private def apiInfo(info: Info): SInfo = {
    val inf = new SInfo
    inf.title(info.title)
      .description(info.desc)
      .version(info.version)
      .termsOfService(info.termsOfService)
  }

  private def apiTags(tags: Iterable[Tag]): JList[STag] = {
    val ret = new JArrayList[STag](tags.size)
    for (t <- tags) {
      val tag = new STag()
      tag.setName(t.name)
      tag.setDescription(t.desc)
      ret.add(tag)
    }
    ret
  }

  private def apiServers(config: Config): JList[SServer] = {
    val servers = config.servers
    val ret = new JArrayList[SServer](servers.size)
    for (s <- servers) {
      val serverVars = new ServerVariables
      for ((name, variable) <- s.vars) {
        val serverVar = new ServerVariable
        val enums = new JArrayList[String](variable.enum.size)
        variable.enum.foreach(enums.add)
        serverVar.setEnum(enums)
        serverVar.setDefault(variable.default)
        serverVar.setDescription(variable.desc)
        serverVar.setExtensions(variable.extensions.asJava)
        serverVars.addServerVariable(name, serverVar)
      }
      val ss = new SServer()
      ss.setUrl(s.url)
      ss.setDescription(s.desc)
      ss.setVariables(serverVars)
      ss.setExtensions(s.extensions.asJava)
      ret.add(ss)
    }
    ret
  }

  def apply(route: Route): PathItem = {
    if (route.methods.size != 1) {
      throw new IllegalArgumentException
    }
    doc(route.methods.head, route.path)
  }

}
