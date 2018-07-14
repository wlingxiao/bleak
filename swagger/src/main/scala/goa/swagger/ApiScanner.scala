package goa.swagger

import goa.swagger.util.SwaggerContext
import io.swagger.annotations.Api
import io.swagger.config._
import io.swagger.models.{Contact, Info, License, Scheme, Swagger}
import org.apache.commons.lang3.StringUtils
import java.util.{Set => JSet}

class ApiScanner extends Scanner with SwaggerConfig {

  private def updateInfoFromConfig(swagger: Swagger): Swagger = {
    val info = new Info()
    val playSwaggerConfig = GoaConfigFactory.getConfig

    if (StringUtils.isNotBlank(playSwaggerConfig.description)) {
      info.description(playSwaggerConfig.description)
    }

    if (StringUtils.isNotBlank(playSwaggerConfig.title)) {
      info.title(playSwaggerConfig.title)
    } else {
      info.title("")
    }

    if (StringUtils.isNotBlank(playSwaggerConfig.version)) {
      info.version(playSwaggerConfig.version)
    }

    if (StringUtils.isNotBlank(playSwaggerConfig.termsOfServiceUrl)) {
      info.termsOfService(playSwaggerConfig.termsOfServiceUrl)
    }

    if (playSwaggerConfig.contact != null) {
      info.contact(new Contact()
        .name(playSwaggerConfig.contact))
    }
    if (playSwaggerConfig.license != null && playSwaggerConfig.licenseUrl != null) {
      info.license(new License()
        .name(playSwaggerConfig.license)
        .url(playSwaggerConfig.licenseUrl))
    }
    swagger.info(info)
  }

  override def configure(swagger: Swagger): Swagger = {
    val playSwaggerConfig = GoaConfigFactory.getConfig
    if (playSwaggerConfig.schemes != null) {
      for (s <- playSwaggerConfig.schemes) swagger.scheme(Scheme.forValue(s))
    }
    updateInfoFromConfig(swagger)
    swagger.host(playSwaggerConfig.host)
    swagger.basePath(playSwaggerConfig.basePath)

  }

  override def getFilterClass: String = {
    null
  }

  override def classes(): JSet[Class[_]] = {
    import collection.JavaConverters._
    val routes = RouteFactory.getRoute.getAll.asScala
    val controllers = routes.map { case (_, route) =>
      route.target.map(x => x.getClass.getName).getOrElse("")
    }.toList.distinct
    val list = controllers.collect {
      case className: String if {
        try {
          val ret = SwaggerContext.loadClass(className).getAnnotation(classOf[Api]) != null
          ret
        } catch {
          case ex: Exception =>
            false
        }
      } => SwaggerContext.loadClass(className)
    }

    list.toSet.asJava
  }

  override def getPrettyPrint: Boolean = true

  override def setPrettyPrint(x: Boolean) {}

}
