package goa
package swagger2

import goa.util.RicherString._
import io.swagger.models.properties.Property
import io.swagger.models.{Operation, Scheme, Swagger, Tag}
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class SwaggerApi(val route: Router, val api: Api) {

  private var _apiOperation: ApiOperation = _

  private var _apiResponse: ApiResponse = _

  private val _apiParams: ListBuffer[ApiParam] = ListBuffer()

  def apiResponse(code: Int, message: String): SwaggerApi = {
    _apiResponse = ApiResponse(code, message)
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
    this
  }

  def apiParam(params: ApiParam*): SwaggerApi = {
    _apiParams ++= params
    this
  }

  private val swagger: Swagger = new Swagger

  def toSwagger: Swagger = {
    val readable = api != null && api.hidden
    var consumes = new Array[String](0)
    var produces = new Array[String](0)
    val tags = mutable.HashMap[String, Tag]()
    if (readable) {
      val tagStrings = extractTags()
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

      val httpMethods = extractOperationMethod()


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

  private def extractTags(): Set[String] = {
    Set.empty
  }

  private def parseSchemes(schemes: String): Set[Scheme] = {
    Set.empty
  }

  private def extractOperationMethod(): Seq[String] = {
    if (_apiOperation.httpMethod.nonEmpty) {
      _apiOperation.httpMethod
    } else {
      route.methods.map(_.name)
    }
  }

  def apiOperation: ApiOperation = _apiOperation

  private def parseOperation(): Operation = {
    val op = new Operation
    var opId = route.name
    op.setOperationId(opId)
    if (apiOperation != null) {
      if (apiOperation.hidden) {
        return null
      }
      if (apiOperation.nickname.nonBlank) {
        opId = apiOperation.nickname
      }
      var defaultResponseHeaders = parseResponseHeaders(apiOperation.responseHeaders)
      op.summary(apiOperation.value).description(apiOperation.notes)
    }
    op
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

}

trait SwaggerSupport {

  def doc(route: Router)(implicit api: Api): SwaggerApi = {
    new SwaggerApi(route, api)
  }

}
