package bleak
package swagger3

import io.swagger.v3.oas.models.{Components, OpenAPI, Paths}
import io.swagger.v3.oas.models.info.{Info => SInfo}
import io.swagger.v3.oas.models.tags.{Tag => STag}
import java.util.{ArrayList => JArrayList, List => JList, HashMap => JHashMap}

import io.swagger.v3.oas.models.servers.{ServerVariable, ServerVariables, Server => SServer}

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConverters._

class Api {

  private val pathItemBuilders = new ArrayBuffer[PathItemBuilder]()

  def doc(routeName: String): PathItemBuilder = {
    val method = routeName.split(" ")(0).toLowerCase()
    val path = routeName.split(" ")(1)
    doc(path, method)
  }

  private def doc(method: String, path: String): PathItemBuilder = {
    val pathItemBuilder = PathItemBuilder(path, method)
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

}
