package goa
package swagger2

import java.lang.reflect.Type

import goa.util.RicherString._
import io.swagger.converter.ModelConverters
import io.swagger.models.properties.{Property, RefProperty}
import io.swagger.models._
import io.swagger.models.parameters.{BodyParameter, Parameter, PathParameter, QueryParameter}
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.JavaConverters._

class SwaggerApi(val route: Router, val api: Api, apiConfig: ApiConfig) {

  private var _apiOperation: ApiOperation = _

  private var _apiResponses = ListBuffer[ApiResponse]()

  private val _apiParams: ListBuffer[ApiParam] = ListBuffer()

  val SUCCESSFUL_OPERATION = ""

  def apiResponse(code: Int, message: String): SwaggerApi = {
    _apiResponses += ApiResponse(code, message)
    this
  }

  def apiResponse(responses: ApiResponse*): SwaggerApi = {
    this
  }

  def apiOperation(summary: String, notes: String = ""): SwaggerApi = {
    _apiOperation = ApiOperation(summary, notes = notes)
    this
  }

  def apiOperation(op: ApiOperation): SwaggerApi = {
    _apiOperation = op
    this
  }

  def apiParam(params: ApiParam*): SwaggerApi = {
    _apiParams ++= params
    this
  }

  def apiParams: ListBuffer[ApiParam] = _apiParams

  var sg: Swagger = _

  def toSwagger(swagger: Swagger): Swagger = {
    sg = swagger
    val readable = api != null && !api.hidden
    var consumes = new Array[String](0)
    var produces = new Array[String](0)
    val tags = mutable.HashMap[String, Tag]()
    if (readable) {
      val tagStrings = extractTags(api)
      tagStrings.foreach { tagString =>
        val tag = new Tag().name(tagString)
        tags.put(tagString, tag)
      }
      tags.keySet.foreach { tagName =>
        swagger.tag(tags(tagName))
      }
      if (api.produces.nonBlank) {
        produces = toArray(api.produces)
      }
      if (api.consumes.nonBlank) {
        consumes = toArray(api.consumes)
      }

      val operationPath = getPathFromRoute(route.path, apiConfig.basePath)

      val httpMethod = extractOperationMethod()
      var operation: Operation = null
      if (httpMethod != null) {
        operation = parseOperation()
        if (apiOperation != null) for (tag <- apiOperation.tags) {
          if (tag.nonBlank) {
            operation.tag(tag)
            swagger.tag(new Tag().name(tag))
          }
        }

        if (operation.getConsumes == null) {
          consumes.foreach(operation.consumes)
        }
        if (operation.getProduces == null) {
          produces.foreach(operation.produces)
        }
        if (operation.getTags == null) {
          tags.keySet.foreach(operation.tag)
        }
        if (operation.getSecurity == null) {
        }
      }

      var path = swagger.getPath(operationPath)
      if (path == null) {
        path = new Path
        swagger.path(operationPath, path)
      }
      path.set(httpMethod, operation)

    }
    swagger
  }

  private def toArray(csString: String): Array[String] = {
    if (StringUtils.isEmpty(csString)) {
      Array(csString)
    } else {
      csString.split(",").map(_.trim)
    }
  }

  private def extractTags(api: Api): Set[String] = {
    val output = mutable.LinkedHashSet[String]()
    var hasExplicitTags = false
    api.tags.foreach { tag =>
      if (tag.nonEmpty) {
        hasExplicitTags = true
        output += tag
      }
    }
    if (!hasExplicitTags) {
      val tagString = api.value.replace("/", "")
      if (tagString.nonEmpty) {
        output += tagString
      }
    }
    output.toSet
  }

  private def parseSchemes(schemes: String): Set[Scheme] = {
    Set.empty
  }

  private def extractOperationMethod(): String = {
    if (_apiOperation.httpMethod.nonEmpty) {
      _apiOperation.httpMethod.toLowerCase()
    } else {
      route.methods.head.name.toLowerCase()
    }
  }

  def apiOperation: ApiOperation = _apiOperation

  private def parseOperation(): Operation = {
    val op = new Operation
    var opId = route.name
    op.setOperationId(opId)
    var responseType: Type = null
    var responseContainer: String = null
    if (apiOperation != null) {
      if (apiOperation.hidden) {
        return null
      }
      if (apiOperation.nickname.nonBlank) {
        opId = apiOperation.nickname
      }
      var defaultResponseHeaders = parseResponseHeaders(apiOperation.responseHeaders)
      op.summary(apiOperation.value).description(apiOperation.notes)
      if (apiOperation.response != null) {
        responseType = apiOperation.response
      }
      if (apiOperation.responseContainer.nonBlank) {
        responseContainer = apiOperation.responseContainer
      }
      if (apiOperation.authorizations != null) {
        val securities = ArrayBuffer[SecurityRequirement]()
        for (auth <- apiOperation.authorizations) {
          if (auth.value.nonBlank) {
            val security = new SecurityRequirement
            security.setName(auth.value)
            val scopes = auth.scopes
            for (scope <- scopes) {
              if (scope.scope.nonBlank) {
                security.addScope(scope.scope)
              }
            }
            securities += security
          }
        }
        if (securities.nonEmpty) {
          securities.foreach(op.security)
        }
      }
      if (apiOperation.consumes != null) {
        op.consumes(apiOperation.consumes.toList.asJava)
      }
      if (apiOperation.produces != null) {
        op.produces(apiOperation.produces.toList.asJava)
      }
    }

    if (responseType != null) {
      val property = ModelConverters.getInstance.readAsProperty(responseType)
      if (property != null) {
        val responseProperty = ContainerWrapper.wrapContainer(responseContainer, property)
        val responseCode = if (apiOperation == null) 200
        else apiOperation.code
        op.response(responseCode, new Response().description(SUCCESSFUL_OPERATION).schema(responseProperty))
        appendModels(responseType)
      }

    }

    if (apiOperation != null && apiOperation.responseReference.nonBlank) {
      val response = new Response().description(SUCCESSFUL_OPERATION)
      response.schema(new RefProperty(apiOperation.responseReference))
      op.addResponse(apiOperation.code.toString, response)
    }
    op.setDeprecated(apiOperation.deprecated)

    readParam().foreach(op.parameter)
    op
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

  private def parseResponseHeaders(headers: Seq[ResponseHeader]): mutable.HashMap[String, Property] = {
    val responseHeaders = mutable.HashMap[String, Property]()
    if (headers != null && headers.nonEmpty) {
      for (header <- headers) {
        val name = header.name
        if (name.nonEmpty) {
          val description = header.description
          val cls = header.response
        }
      }
    }
    responseHeaders
  }

  private def readParam(): Seq[Parameter] = {
    apiParams.map {
      case pathParam: PathParam[_] =>
        val pathParameter = new PathParameter
        pathParameter.setRequired(pathParam.required)
        pathParameter.setReadOnly(pathParam.readOnly)
        pathParameter.setDescription(pathParam.desc)
        pathParameter.setName(pathParam.name)
        appendModels(pathParam.tpe.runtimeClass)
        pathParameter
      case queryParam: QueryParam[_] =>
        val queryParameter = new QueryParameter
        queryParameter.setRequired(queryParam.required)
        queryParameter.setReadOnly(queryParam.readOnly)
        queryParameter.setDescription(queryParam.desc)
        queryParameter.setName(queryParam.name)
        appendModels(queryParam.tpe.runtimeClass)
        queryParameter
      case bodyParam: BodyParam[_] =>
        val bodyParameter = new BodyParameter
        val rc = bodyParam.tpe.runtimeClass
        val models = ModelConverters.getInstance.readAll(rc)
        bodyParameter.schema(models.get(rc.getSimpleName))
        bodyParameter.setName(bodyParam.name)
        bodyParameter.description(bodyParam.desc)
        bodyParameter
      case _ =>
        throw new IllegalArgumentException
    }
  }

  private def appendModels(`type`: Type): Unit = {
    val models: java.util.Map[String, Model] = modelConverters.readAll(`type`)
    for (entry <- models.entrySet.asScala) {
      sg.model(entry.getKey, entry.getValue)
    }
  }

  private def modelConverters = ModelConverters.getInstance()

}

trait SwaggerSupport {

  private val apis = ListBuffer[SwaggerApi]()

  def apiConfig: ApiConfig = ApiConfig(basePath = "")

  def doc(route: Router)(implicit api: Api): SwaggerApi = {
    val sa = new SwaggerApi(route, api, apiConfig)
    apis += sa
    sa
  }

  def convertToSwagger(): Swagger = {
    val swagger = new Swagger
    apis.foreach { api =>
      api.toSwagger(swagger)
    }
    swagger
  }

}
