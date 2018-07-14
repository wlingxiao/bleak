package goa.swagger

import java.lang.reflect.Method
import java.util.{EnumSet, List => JList, Set => JSet}

import goa.Route
import io.swagger.annotations.{Api, ApiOperation, SwaggerDefinition}
import io.swagger.models.parameters.{Parameter, PathParameter, QueryParameter}
import io.swagger.models._
import io.swagger.util.{BaseReaderUtils, ReflectionUtils}
import org.apache.commons.lang3.StringUtils

import collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

private class SwaggerReader(apiConfig: ApiConfig, routes: RouteHolder) {

  private val swagger: Swagger = new Swagger

  private val goaReader = new GoaReader(null, null, null)

  def read(classes: JSet[Class[_]]): Swagger = {
    classes.asScala.foreach { cls =>
      val swaggerDefinition = cls.getAnnotation(classOf[SwaggerDefinition])
      if (swaggerDefinition != null) {
        readSwaggerConfig(cls, swaggerDefinition)
      }
      read(cls, readHidden = false)
    }
    swagger
  }

  private def read(cls: Class[_], readHidden: Boolean): Swagger = {
    val api = cls.getAnnotation(classOf[Api])
    val tags = mutable.HashMap[String, Tag]()
    val securities = ListBuffer[SecurityRequirement]()
    var consumes = new Array[String](0)
    var produces = new Array[String](0)
    val globalSchemes: JSet[Scheme] = EnumSet.noneOf(classOf[Scheme])
    val readable = (api != null && readHidden) || (api != null && !api.hidden())
    if (readable) {
      val tagStrings = extractTags(api)
      tagStrings.foreach { tagString =>
        val tag = new Tag().name(tagString)
        tags.put(tagString, tag)
      }
      tags.keySet.foreach { tagName =>
        swagger.tag(tags(tagName))
      }
      if (api.produces().nonEmpty) {
        produces = toArray(api.produces())
      }
      if (api.consumes().nonEmpty) {
        consumes = toArray(api.consumes())
      }
      globalSchemes.addAll(parseSchemes(api.protocols))
      val authorizations = api.authorizations
      for (auth <- authorizations) {
        if (auth.value() != null && auth.value().nonEmpty) {
          val security = new SecurityRequirement
          //security.setName(auth.value)
          auth.scopes.foreach { scope =>
            if (scope.scope != null && scope.scope.nonEmpty) security.addScope(scope.scope)
          }
          securities += security
        }
      }

      val methods = cls.getMethods
      methods.filter(method => {
        !ReflectionUtils.isOverriddenMethod(method, cls)
      }).filter { method =>
        val fullMethodName = getFullMethodName(cls, method)
        routes.exists(fullMethodName)
      }.foreach { method =>
        val fullMethodName = getFullMethodName(cls, method)
        val route = routes.get(fullMethodName)
        val operationPath = goaReader.getPathFromRoute(route.path, apiConfig.basePath)
        if (operationPath != null) {
          val apiOperation = ReflectionUtils.getAnnotation(method, classOf[ApiOperation])
          val httpMethod = goaReader.extractOperationMethod(apiOperation, method, route)
          var operation: Operation = null
          if (apiOperation != null || httpMethod != null) {
            operation = goaReader.parseMethod(cls, method, route)
          }
          if (operation == null) {
            // TODO continue
          }
          if (apiOperation != null) {
            for (scheme <- parseSchemes(apiOperation.protocols).asScala) {
              operation.scheme(scheme)
            }
          }
          if (operation.getSchemes == null || operation.getSchemes.isEmpty) {
            for (scheme <- globalSchemes.asScala) {
              operation.scheme(scheme)
            }
          }
          if (httpMethod != null) {
            if (apiOperation != null) {
              for (tag <- apiOperation.tags()) {
                if (tag.nonEmpty) {
                  operation.tag(tag)
                  swagger.tag(new Tag().name(tag))
                }
              }
              operation.getVendorExtensions.putAll(BaseReaderUtils.parseExtensions(apiOperation.extensions))
            }
            if (operation.getConsumes == null) {
              for (mediaType <- consumes) {
                operation.consumes(mediaType)
              }
            }
            if (operation.getProduces == null) {
              for (mediaType <- produces) {
                operation.produces(mediaType)
              }
            }
            if (operation.getTags == null) {
              tags.keySet.foreach(tagString => operation.tag(tagString))
            }
            if (operation.getSecurity == null) {
              for (security <- securities) {
                operation.security(security)
              }
            }
            var path = swagger.getPath(operationPath)
            if (path == null) {
              path = new Path
              swagger.path(operationPath, path)
            }
            path.set(httpMethod, operation)
            goaReader.readImplicitParameters(method, operation, cls)
          }
        }
      }
    }
    swagger
  }

  private def readSwaggerConfig(cls: Class[_], definition: SwaggerDefinition): Unit = {
    // TODO readSwaggerConfig
  }

  private def extractTags(api: Api): Set[String] = {
    val output = mutable.LinkedHashSet[String]()
    var hasExplicitTags = false
    api.tags().foreach { tag =>
      if (tag.nonEmpty) {
        hasExplicitTags = true
        output += tag
      }
    }
    if (!hasExplicitTags) {
      val tagString = api.value().replace("/", "")
      if (tagString.nonEmpty) {
        output += tagString
      }
    }
    output.toSet
  }

  private def toArray(csString: String): Array[String] = {
    if (StringUtils.isEmpty(csString)) {
      Array(csString)
    } else {
      csString.split(",").map(_.trim)
    }
  }

  private def parseSchemes(schemes: String): JSet[Scheme] = {
    val result = java.util.EnumSet.noneOf(classOf[Scheme])
    StringUtils.trimToEmpty(schemes).split(",").foreach { item =>
      val scheme = Scheme.forValue(StringUtils.trimToNull(item))
      if (scheme != null) result.add(scheme)
    }
    result
  }

  private def getFullMethodName(clazz: Class[_], method: Method): String = {
    if (clazz.getCanonicalName.contains("$")) {
      clazz.getCanonicalName + "." + method.getName
    } else {
      clazz.getCanonicalName + "$." + method.getName
    }
  }

}

object SwaggerReader {
  def readParam(parameters: JList[Parameter], route: Route): Unit = {
    route.params.foreach { x =>
      x.paramType match {
        case Some(p) =>
          p match {
            case "PathParam" =>
              val pathParameter = new PathParameter
              pathParameter.setName(x.name.get)
              pathParameter.setType("integer")
              parameters.add(pathParameter)
            case "QueryParam" =>
              val queryParameter = new QueryParameter
              queryParameter.setName(x.name.get)
              queryParameter.setType("string")
              parameters.add(queryParameter)
          }
        case None =>
      }
    }
  }

}