package bleak
package swagger3

import io.swagger.v3.oas.models.{Components, OpenAPI, Paths}
import io.swagger.v3.oas.models.info.{Info => SInfo}
import io.swagger.v3.oas.models.tags.{Tag => STag}
import java.util.{ArrayList => JArrayList, List => JList}

import scala.collection.mutable.ArrayBuffer

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

}
