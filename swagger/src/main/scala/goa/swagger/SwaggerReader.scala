package goa.swagger

import java.lang.reflect.{Method, Type}
import java.util
import java.util.regex.Pattern
import java.util.{Collections, EnumSet, List => JList, Set => JSet}

import goa.Route
import goa.annotation.{PathParam, QueryParam, RouteParam}
import io.swagger.annotations.{Contact => _, ExternalDocs => _, Info => _, License => _, Tag => _, _}
import io.swagger.converter.ModelConverters
import io.swagger.models._
import io.swagger.models.parameters._
import io.swagger.models.properties._
import io.swagger.util._
import org.apache.commons.lang3.StringUtils

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer


private class SwaggerReader(apiConfig: ApiConfig, routes: RouteHolder) {

  val SUCCESSFUL_OPERATION = ""

  private val swagger: Swagger = new Swagger

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
    val globalSchemes: JSet[Scheme] = java.util.EnumSet.noneOf(classOf[Scheme])
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
        val operationPath = getPathFromRoute(route.path, apiConfig.basePath)
        if (operationPath != null) {
          val apiOperation = ReflectionUtils.getAnnotation(method, classOf[ApiOperation])
          val httpMethod = extractOperationMethod(apiOperation, method, route)
          var operation: Operation = null
          if (apiOperation != null || httpMethod != null) {
            operation = parseMethod(cls, method, route)
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
            readImplicitParameters(method, operation, cls)
          }
        }
      }
    }
    swagger
  }

  private def parseMethod(cls: Class[_], method: Method, route: Route): Operation = {
    val operation = new Operation
    val apiOperation = ReflectionUtils.getAnnotation(method, classOf[ApiOperation])
    val responseAnnotation = ReflectionUtils.getAnnotation(method, classOf[ApiResponses])
    var operationId = method.getName
    operation.operationId(operationId)
    var responseContainer: String = null
    var responseType: Type = null
    var defaultResponseHeaders = mutable.HashMap[String, Property]()
    if (apiOperation != null) {
      if (apiOperation.hidden()) return null
      if (apiOperation.nickname().nonEmpty) {
        operationId = apiOperation.nickname()
      }
      defaultResponseHeaders = parseResponseHeaders(apiOperation.responseHeaders())
      operation.summary(apiOperation.value).description(apiOperation.notes)
      if (apiOperation.response != null && !isVoid(apiOperation.response)) responseType = apiOperation.response
      if (!("" == apiOperation.responseContainer)) responseContainer = apiOperation.responseContainer
      if (apiOperation.authorizations != null) {
        val securities = new util.ArrayList[SecurityRequirement]
        for (auth <- apiOperation.authorizations) {
          if (auth.value != null && !("" == auth.value)) {
            val security = new SecurityRequirement
            security.setName(auth.value)
            val scopes = auth.scopes
            for (scope <- scopes) {
              if (scope.scope != null && !("" == scope.scope)) security.addScope(scope.scope)
            }
            securities.add(security)
          }
        }
        if (securities.size > 0) securities.forEach(operation.security)
      }
      if (apiOperation.consumes != null && !apiOperation.consumes.isEmpty) operation.consumes(toArray(apiOperation.consumes).toList.asJava)
      if (apiOperation.produces != null && !apiOperation.produces.isEmpty) operation.produces(toArray(apiOperation.produces).toList.asJava)
    }
    if (apiOperation != null && StringUtils.isNotEmpty(apiOperation.responseReference)) {
      val response = new Response().description(SUCCESSFUL_OPERATION)
      response.schema(new RefProperty(apiOperation.responseReference))
      operation.addResponse(String.valueOf(apiOperation.code), response)
    } else if (responseType == null) {
      responseType = method.getGenericReturnType
    }
    if (isValidResponse(responseType)) {
      val property = ModelConverters.getInstance.readAsProperty(responseType)
      if (property != null) {
        val responseProperty = ContainerWrapper.wrapContainer(responseContainer, property)
        val responseCode = if (apiOperation == null) 200
        else apiOperation.code
        operation.response(responseCode, new Response().description(SUCCESSFUL_OPERATION).schema(responseProperty).headers(defaultResponseHeaders.asJava))
        appendModels(responseType)
      }
    }
    operation.operationId(operationId)

    if (responseAnnotation != null) for (apiResponse <- responseAnnotation.value) {
      val responseHeaders = parseResponseHeaders(apiResponse.responseHeaders)
      val response = new Response().description(apiResponse.message).headers(responseHeaders.asJava)
      if (apiResponse.code == 0) operation.defaultResponse(response)
      else operation.response(apiResponse.code, response)
      if (StringUtils.isNotEmpty(apiResponse.reference)) response.schema(new RefProperty(apiResponse.reference))
      else if (!isVoid(apiResponse.response)) {
        responseType = apiResponse.response
        val property = ModelConverters.getInstance.readAsProperty(responseType)
        if (property != null) {
          response.schema(ContainerWrapper.wrapContainer(apiResponse.responseContainer, property))
          appendModels(responseType)
        }
      }
    }

    if (ReflectionUtils.getAnnotation(method, classOf[Deprecated]) != null) operation.setDeprecated(true)
    val parameters = getParameters(cls, method, route)
    parameters.forEach(operation.parameter)
    if (operation.getResponses == null) {
      val response = new Response().description(SUCCESSFUL_OPERATION)
      operation.defaultResponse(response)
    }
    operation
  }

  private def getParameters(cls: Class[_], method: Method, route: Route): util.ArrayList[Parameter] = {
    val parameters = new util.ArrayList[Parameter]
    readParam(parameters, route)
    parameters
  }

  def readParam(parameters: JList[Parameter], route: Route): Unit = {
    import scala.reflect.runtime.universe._
    route.params.foreach {
      case RouteParam(param: Option[_], symbol) =>
        param match {
          case Some(p) =>
            p match {
              case pathParam: PathParam =>
                val pathParameter = new PathParameter
                pathParameter.setName(pathParam.value)
                pathParameter.setType(symbol.info.toString.toLowerCase)
                pathParameter.setRequired(pathParam.required)
                parameters.add(pathParameter)
              case q: QueryParam =>
                val queryParameter = new QueryParameter
                queryParameter.setName(q.value)
                queryParameter.setRequired(q.required)
                val t = symbol match {
                  case m if m.info <:< typeOf[Long] => "integer"
                  case m if m.info <:< typeOf[String] => "string"
                  case _ => throw new IllegalStateException(symbol.toString)
                }
                queryParameter.setType(t)
                parameters.add(queryParameter)
              case _ => throw new IllegalStateException()
            }
          case _ => throw new IllegalStateException()
        }
    }
  }

  private def isValidResponse(`type`: Type): Boolean = {
    if (`type` == null) return false
    val javaType = Json.mapper.getTypeFactory.constructType(`type`)
    if (isVoid(javaType)) return false
    val cls = javaType.getRawClass
    !isResourceClass(cls)
  }

  private def isResourceClass(cls: Class[_]): Boolean = cls.getAnnotation(classOf[Api]) != null

  private def isVoid(`type`: Type) = {
    val cls = Json.mapper.getTypeFactory.constructType(`type`).getRawClass
    classOf[Void].isAssignableFrom(cls) || Void.TYPE.isAssignableFrom(cls)
  }

  private def parseResponseHeaders(headers: Array[ResponseHeader]): mutable.HashMap[String, Property] = {
    var responseHeaders = mutable.HashMap[String, Property]()
    if (headers != null && headers.nonEmpty) {
      for (header <- headers) {
        val name = header.name()
        if (name.nonEmpty) {
          val description = header.description()
          val cls = header.response()
          if (isVoid(cls)) {
            val property = ModelConverters.getInstance().readAsProperty(cls)
            if (property != null) {
              val responseProperty = ContainerWrapper.wrapContainer(header.responseContainer(), property, ARRAY, LIST, SET)
              responseProperty.setDescription(description)
              responseHeaders.put(name, responseProperty)
              appendModels(cls)
            }
          }
        }
      }
    }
    responseHeaders
  }

  private def appendModels(`type`: Type): Unit = {
    val models: util.Map[String, Model] = ModelConverters.getInstance.readAll(`type`)
    for (entry <- models.entrySet.asScala) {
      swagger.model(entry.getKey, entry.getValue)
    }
  }

  private def getPathFromRoute(pathPattern: String, basePath: String): String = {
    val sb = new StringBuilder
    sb.append(pathPattern)
    val basePathFilter = new StringBuilder(basePath)
    if (basePath.startsWith("/")) basePathFilter.deleteCharAt(0)
    if (!basePath.endsWith("/")) basePathFilter.append("/")
    val basePathString = basePathFilter.toString
    val pathPatternString = sb.toString
    val operationPath = new StringBuilder
    if ((pathPatternString.startsWith("/") && pathPatternString.startsWith(basePathString, 1)) || pathPatternString.startsWith(basePathString)) operationPath.append(pathPatternString.replaceFirst(basePathString, ""))
    else operationPath.append(pathPatternString)
    if (!operationPath.toString.startsWith("/")) operationPath.insert(0, "/")
    operationPath.toString
  }

  private def extractOperationMethod(apiOperation: ApiOperation, method: Method, route: Route): String = {
    var httpMethod: String = null
    if (route != null) try
      httpMethod = route.method.name.toLowerCase
    catch {
      case e: Exception =>
    }
    if (httpMethod == null) if (!StringUtils.isEmpty(apiOperation.httpMethod)) httpMethod = apiOperation.httpMethod
    httpMethod
  }

  private def readSwaggerConfig(cls: Class[_], definition: SwaggerDefinition): Unit = {
    if (definition.basePath().nonEmpty) {
      swagger.setBasePath(definition.basePath())
    }
    if (definition.host().nonEmpty) {
      swagger.host(definition.host())
    }
    readInfoConfig(definition)
    definition.consumes().foreach { consume =>
      if (StringUtils.isNoneBlank(consume)) swagger.addConsumes(consume)
    }
    definition.produces().foreach { produce =>
      if (StringUtils.isNoneBlank(produce)) swagger.addProduces(produce)
    }
    if (definition.externalDocs().value().nonEmpty) {
      var externalDocs = swagger.getExternalDocs
      if (externalDocs == null) {
        externalDocs = new ExternalDocs()
        swagger.setExternalDocs(externalDocs)
      }
      externalDocs.setDescription(definition.externalDocs().value())
      if (definition.externalDocs().url().nonEmpty) {
        externalDocs.setUrl(definition.externalDocs().url())
      }
    }
    definition.tags().foreach { tagConfig =>
      if (tagConfig.name().nonEmpty) {
        val tag = new Tag
        tag.setName(tagConfig.name())
        tag.setDescription(tagConfig.description())
        if (tagConfig.externalDocs().value().nonEmpty) {
          tag.setExternalDocs(new ExternalDocs(tagConfig.externalDocs().value(), tagConfig.externalDocs().url()))
        }
        tag.getVendorExtensions.putAll(BaseReaderUtils.parseExtensions(tagConfig.extensions))
        swagger.addTag(tag)
      }
    }
    definition.schemes().foreach { scheme =>
      if (scheme != SwaggerDefinition.Scheme.DEFAULT) {
        swagger.addScheme(Scheme.forValue(scheme.name()))
      }
    }
  }

  private def readInfoConfig(definition: SwaggerDefinition): Unit = {
    val infoConfig = definition.info()
    var info = swagger.getInfo
    if (info == null) {
      info = new Info
      swagger.setInfo(info)
    }
    if (infoConfig.description().nonEmpty) {
      info.setDescription(infoConfig.description())
    }
    if (infoConfig.termsOfService().nonEmpty) {
      info.setTermsOfService(info.getTermsOfService)
    }
    if (infoConfig.title.nonEmpty) {
      info.setTitle(infoConfig.title)
    }
    if (infoConfig.version.nonEmpty) {
      info.setVersion(infoConfig.version)
    }
    if (infoConfig.contact().name().nonEmpty) {
      var contact = info.getContact
      if (contact == null) {
        contact = new Contact
        info.setContact(contact)
      }
      contact.setName(infoConfig.contact.name)
      if (infoConfig.contact.email.nonEmpty) {
        contact.setEmail(infoConfig.contact.email)
      }
      if (infoConfig.contact.url.nonEmpty) {
        contact.setUrl(infoConfig.contact.url)
      }
    }
    if (infoConfig.license.name.nonEmpty) {
      var license = info.getLicense
      if (license == null) {
        license = new License
        info.setLicense(license)
      }
      license.setName(infoConfig.license.name)
      if (infoConfig.license.url.nonEmpty) {
        license.setUrl(infoConfig.license.url)
      }
    }
    info.getVendorExtensions.putAll(BaseReaderUtils.parseExtensions(infoConfig.extensions))
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

  private def readImplicitParameters(method: Method, opration: Operation, cls: Class[_]): Unit = {
    val implicitParams = method.getAnnotation(classOf[ApiImplicitParams])
    if (implicitParams != null && implicitParams.value().length > 0) {
      implicitParams.value().foreach { param =>
        val p = readImplicitParam(param, cls)
        if (p != null) {
          opration.addParameter(p)
        }
      }
    }
  }

  private def readImplicitParam(param: ApiImplicitParam, cls: Class[_]): Parameter = {
    val p: Parameter = if (param.paramType().equalsIgnoreCase("path")) new PathParameter
    else if (param.paramType().equalsIgnoreCase("query")) new QueryParameter
    else if (param.paramType().equalsIgnoreCase("form") || param.paramType().equalsIgnoreCase("formData")) {
      new FormParameter
    } else if (param.paramType().equalsIgnoreCase("body")) null
    else if (param.paramType().equalsIgnoreCase("header")) new HeaderParameter
    else null
    var `type`: Type = null
    if (param.dataType().nonEmpty && !"file".equalsIgnoreCase(param.dataType()) && !"array".equalsIgnoreCase(param.dataType())) {
      `type` = typeFromString(param.dataType(), cls)
    }
    val a = if (`type` == null) classOf[String] else `type`
    val result = ParameterProcessor.applyAnnotations(swagger, p, a, Collections.singletonList(param))
    if (result.isInstanceOf[AbstractSerializableParameter[_]] && `type` != null) {
      val schema = createProperty(`type`)
      p.asInstanceOf[AbstractSerializableParameter[_]].setProperty(schema)
    }
    result
  }

  private def typeFromString(`type`: String, cls: Class[_]): Type = {
    val primitive = PrimitiveType.fromName(`type`)
    if (primitive != null) return primitive.getKeyClass
    try {
      val routeType = getOptionTypeFromString(`type`, cls)
      if (routeType != null) return routeType
      return Thread.currentThread.getContextClassLoader.loadClass(`type`)
    } catch {
      case e: Exception =>
    }
    null
  }

  private def getOptionTypeFromString(simpleTypeName: String, cls: Class[_]): Type = {
    if (simpleTypeName == null) return null
    val regex = "(Option|scala\\.Option)\\s*\\[\\s*(Int|Long|Float|Double|Byte|Short|Char|Boolean)\\s*\\]\\s*$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(simpleTypeName)
    if (matcher.find) {
      val enhancedType = matcher.group(2)
      OptionTypeResolver.resolveOptionType(enhancedType, cls)
    } else null
  }

  private def createProperty(`type`: Type): Property = enforcePrimitive(ModelConverters.getInstance.readAsProperty(`type`), 0)

  //TODO tailrec
  private def enforcePrimitive(in: Property, level: Int): Property = {
    in match {
      case _: RefProperty => new StringProperty
      case array: ArrayProperty =>
        if (level == 0) {
          array.setItems(enforcePrimitive(array.getItems, level + 1))
        } else new StringProperty()
      case _ => in
    }
    in
  }

  private def toArray(csString: String) = {
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