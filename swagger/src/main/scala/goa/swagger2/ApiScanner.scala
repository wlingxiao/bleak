package goa.swagger2

import java.util.{Set => JSet}

import io.swagger.config._
import io.swagger.models._
import org.apache.commons.lang3.StringUtils

class ApiScanner(apiConfig: ApiConfig, routeHolder: RouteHolder) extends Scanner with SwaggerConfig {

  private def updateInfoFromConfig(swagger: Swagger): Swagger = {
    val info = new Info()
    if (isNotBlank(apiConfig.description)) {
      info.description(apiConfig.description)
    }

    if (isNotBlank(apiConfig.title)) {
      info.title(apiConfig.title)
    } else {
      info.title("")
    }

    if (isNotBlank(apiConfig.version)) {
      info.version(apiConfig.version)
    }

    if (isNotBlank(apiConfig.termsOfServiceUrl)) {
      info.termsOfService(apiConfig.termsOfServiceUrl)
    }

    if (apiConfig.contact != null) {
      info.contact(new Contact()
          .name(apiConfig.contact))
    }
    if (apiConfig.license != null && apiConfig.licenseUrl != null) {
      info.license(new License()
          .name(apiConfig.license)
          .url(apiConfig.licenseUrl))
    }
    swagger.info(info)
  }

  private def isNotBlank(str: String): Boolean = {
    StringUtils.isNotBlank(str)
  }

  override def configure(swagger: Swagger): Swagger = {
    if (apiConfig.schemes != null) {
      for (s <- apiConfig.schemes) swagger.scheme(Scheme.forValue(s))
    }
    updateInfoFromConfig(swagger)
    swagger.host(apiConfig.host)
    swagger.basePath(apiConfig.basePath)

  }

  override def getFilterClass: String = {
    null
  }

  override def classes(): JSet[Class[_]] = {
    null
  }

  override def getPrettyPrint: Boolean = true

  override def setPrettyPrint(x: Boolean) {}

}
