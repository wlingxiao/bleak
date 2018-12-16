package bleak
package swagger2

import java.lang.reflect.Type

import bleak.util.RicherString._
import io.swagger.converter.ModelConverters
import io.swagger.models.properties.{Property, RefProperty}
import io.swagger.models._
import io.swagger.models.parameters._
import org.apache.commons.lang3.StringUtils

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scala.collection.JavaConverters._
import scala.reflect.ClassTag
import scala.reflect.runtime.{universe => ru}

class SwaggerApi(val api: Api, val routeName: String, apiConfig: ApiConfig) {

  private var _apiOperation: ApiOperation[_] = _

  private var _apiResponses = ListBuffer[ApiResponse]()

  private val _apiParams: ListBuffer[ApiParam] = ListBuffer()

  val SUCCESSFUL_OPERATION = "OK"

  def response(code: Int, message: String): SwaggerApi = {
    _apiResponses += ApiResponse(code, message)
    this
  }

  def response(responses: ApiResponse*): SwaggerApi = {
    this
  }

  def operation[T: ru.TypeTag](summary: String, notes: String = ""): SwaggerApi = {
    _apiOperation = ApiOperation[T](summary, notes = notes)
    this
  }

  def operation(op: ApiOperation[_]): SwaggerApi = {
    _apiOperation = op
    this
  }

  def param(params: ApiParam*): SwaggerApi = {
    _apiParams ++= params
    this
  }

  def query[T: ClassTag](name: String,
                         desc: String = "",
                         required: Boolean = false,
                         readOnly: Boolean = false): SwaggerApi = {
    _apiParams += QueryParam(name, desc, required, readOnly)
    this
  }

  def path[T: ClassTag](name: String,
                        desc: String = "",
                        required: Boolean = false,
                        readOnly: Boolean = false): SwaggerApi = {
    _apiParams += PathParam(name, desc, required, readOnly)
    this
  }

  def body[T: ClassTag : ru.TypeTag](name: String = "",
                                     desc: String = "",
                                     required: Boolean = false,
                                     readOnly: Boolean = false): SwaggerApi = {
    _apiParams += BodyParam[T](name, desc, required, readOnly)
    this
  }

  def cookie[T: ClassTag](name: String,
                          desc: String = "",
                          required: Boolean = false,
                          readOnly: Boolean = false): SwaggerApi = {
    _apiParams += CookieParam(name, desc, required, readOnly)
    this
  }

  def header[T: ClassTag](name: String,
                          desc: String = "",
                          required: Boolean = false,
                          readOnly: Boolean = false): SwaggerApi = {
    _apiParams += HeaderParam(name, desc, required, readOnly)
    this
  }

  def form[T: ClassTag](name: String,
                        desc: String = "",
                        required: Boolean = false,
                        readOnly: Boolean = false): SwaggerApi = {
    _apiParams += FormParam(name, desc, required, readOnly)
    this
  }


  def apiParams: ListBuffer[ApiParam] = _apiParams

  var sg: Swagger = _

  def toSwagger(swagger: Swagger, app: Application): Swagger = {
    sg = swagger
    val route = app.routes.filter(x => x.name == routeName).head
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

      val httpMethod = extractOperationMethod(route)
      var operation: Operation = null
      if (httpMethod != null) {
        operation = parseOperation(route)
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

  private def extractOperationMethod(route: Route): String = {
    if (_apiOperation.httpMethod.nonEmpty) {
      _apiOperation.httpMethod.toLowerCase()
    } else {
      route.method.name.toLowerCase()
    }
  }


  private def isCollection(tpe: ru.Type): Boolean = {
    tpe <:< ru.typeOf[Seq[_]] || tpe <:< ru.typeOf[Array[_]] || tpe <:< ru.typeOf[Set[_]]
  }

  private def isBaseType(tpe: ru.Type): Boolean = {
    tpe =:= ru.typeOf[Long]
  }

  private def isMap(tpe: ru.Type): Boolean = {
    tpe <:< ru.typeOf[Map[_, _]]
  }

  def apiOperation: ApiOperation[_] = _apiOperation

  private def parseOperation(route: Route): Operation = {
    val apiOp = apiOperation
    val op = new Operation
    val responseType = _apiOperation.tpe
    if (apiOp != null) {
      if (apiOp.hidden) {
        return null
      }
      val opId = if (apiOp.nickname.nonBlank) apiOp.nickname else route.name
      op.setOperationId(opId)
      var defaultResponseHeaders = parseResponseHeaders(apiOp.responseHeaders)
      op.summary(apiOp.value)
        .description(apiOp.notes)
      if (apiOp.authorizations != null) {
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
      if (apiOp.consumes != null) {
        op.consumes(apiOp.consumes.toList.asJava)
      }
      if (apiOp.produces != null) {
        op.produces(apiOp.produces.toList.asJava)
      }
    }
    val response = new Response().description(SUCCESSFUL_OPERATION)
    if (apiOp != null && apiOp.responseReference.nonBlank) {
      response.responseSchema(new RefModel(apiOperation.responseReference))
    } else {
      response.responseSchema(parseModel(responseType))
    }
    op.addResponse(apiOperation.code.toString, response)
    if (apiOp.deprecated) {
      op.setDeprecated(apiOp.deprecated)
    }
    readParam().foreach(op.parameter)
    op
  }

  private def parseModel(tag: ru.TypeTag[_]): Model = {
    val tpe = tag.tpe
    if (isCollection(tpe)) {
      val typeArg = tag.tpe.typeArgs.head
      val typeArgClass = tag.mirror.runtimeClass(typeArg)
      val property = readProperty(typeArgClass)
      val model = new ArrayModel
      model.setItems(property)
      model
    } else if (isMap(tpe)) {
      null
    } else if (isBaseType(tpe)) {
      val clazz = tag.mirror.runtimeClass(tpe)
      val tf = tpeAndFormat(clazz)
      val model = new ModelImpl
      if (tf != null) {
        model.setType(tf._1)
        model.setFormat(tf._2)
      }
      model
    } else {
      val runtimeClass = tag.mirror.runtimeClass(tpe)
      appendModels(runtimeClass)
      val name = runtimeClass.getSimpleName
      val refModel = new RefModel()
      refModel.set$ref(name)
      refModel
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

  private def tpeAndFormat(clazz: Class[_]): (String, String) = {
    if (clazz.isAssignableFrom(classOf[String])) {
      "string" -> null
    } else if (clazz.isAssignableFrom(classOf[Int])) {
      "integer" -> "int32"
    } else if (clazz.isAssignableFrom(classOf[Long])) {
      "integer" -> "int64"
    } else if (clazz.isAssignableFrom(classOf[Float])) {
      "number" -> "float"
    } else if (clazz.isAssignableFrom(classOf[Double])) {
      "number" -> "double"
    } else if (clazz.isAssignableFrom(classOf[Boolean])) {
      "boolean" -> null
    } else {
      // TODO date and datetime, file
      null
    }
  }

  private def readParam(): Seq[Parameter] = {
    apiParams.map {
      case pathParam: PathParam[_] =>
        val pathParameter = new PathParameter
        pathParameter.setRequired(pathParam.required)
        pathParameter.setReadOnly(pathParam.readOnly)
        pathParameter.setDescription(pathParam.desc)
        pathParameter.setName(pathParam.name)
        val tpeFormat = tpeAndFormat(pathParam.tpe.runtimeClass)
        if (tpeFormat != null) {
          pathParameter.setType(tpeFormat._1)
          pathParameter.setFormat(tpeFormat._2)
        }
        appendModels(pathParam.tpe.runtimeClass)
        pathParameter
      case queryParam: QueryParam[_] =>
        val queryParameter = new QueryParameter
        queryParameter.setRequired(queryParam.required)
        queryParameter.setReadOnly(queryParam.readOnly)
        queryParameter.setDescription(queryParam.desc)
        queryParameter.setName(queryParam.name)
        val tpeFormat = tpeAndFormat(queryParam.tpe.runtimeClass)
        if (tpeFormat != null) {
          queryParameter.setType(tpeFormat._1)
          queryParameter.setFormat(tpeFormat._2)
        }
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
      case cookie: CookieParam[_] =>
        val cookieParameter = new CookieParameter
        cookieParameter.setName(cookie.name)
        cookieParameter.setDescription(cookie.desc)
        cookieParameter.setRequired(cookie.required)
        cookieParameter.setReadOnly(cookie.readOnly)

        val tpeFormat = tpeAndFormat(cookie.tpe.runtimeClass)
        if (tpeFormat != null) {
          cookieParameter.setType(tpeFormat._1)
          cookieParameter.setFormat(tpeFormat._2)
        }

        cookieParameter
      case header: HeaderParam[_] =>
        val headerParameter = new HeaderParameter
        headerParameter.setName(header.name)
        headerParameter.setDescription(header.desc)
        headerParameter.setRequired(header.required)
        headerParameter.setReadOnly(header.readOnly)

        val tpeFormat = tpeAndFormat(header.tpe.runtimeClass)
        if (tpeFormat != null) {
          headerParameter.setType(tpeFormat._1)
          headerParameter.setFormat(tpeFormat._2)
        }

        headerParameter
      case form: FormParam[_] =>
        val formParameter = new FormParameter
        formParameter.setName(form.name)
        formParameter.setDescription(form.desc)
        formParameter.setRequired(form.required)
        formParameter.setReadOnly(form.readOnly)

        val tpeFormat = tpeAndFormat(form.tpe.runtimeClass)
        if (tpeFormat != null) {
          formParameter.setType(tpeFormat._1)
          formParameter.setFormat(tpeFormat._2)
        }

        formParameter
      case _ =>
        throw new IllegalArgumentException
    }
  }

  private val models = mutable.HashMap[String, Model]()

  private val modelConverters = ModelConverters.getInstance()

  private def fromClass(clazz: Class[_]): Model = {
    val name = clazz.getSimpleName
    val model = models.get(name)
    if (model.isDefined) {
      val ref = new RefModel()
      ref.set$ref(name)
      return ref
    }
    null
  }

  private def readProperty(clazz: Class[_]): Property = {
    appendModels(clazz)
    val property = modelConverters.readAsProperty(clazz)
    property
  }

  private def appendModels(tpe: Type): Unit = {
    val models = modelConverters.readAll(tpe)
    if (models != null) {
      this.models ++= models.asScala
    }
    for ((k, v) <- this.models) {
      sg.model(k, v)
    }
  }

}

trait SwaggerSupport {

  val apis = ListBuffer[SwaggerApi]()

  def apiConfig: ApiConfig = ApiConfig(basePath = "")

  def doc(route: Route)(implicit api: Api): SwaggerApi = {
    val sa = new SwaggerApi(api, route.name, apiConfig)
    apis += sa
    sa
  }

  def doc(name: String)(implicit api: Api): SwaggerApi = {
    val sa = new SwaggerApi(api, name, apiConfig)
    apis += sa
    sa
  }


}
