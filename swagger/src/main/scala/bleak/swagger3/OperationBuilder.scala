package bleak
package swagger3

import java.util

import io.swagger.v3.oas.models._
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.parameters.{Parameter, RequestBody}
import io.swagger.v3.oas.models.responses.ApiResponses

import scala.collection.mutable.ArrayBuffer
import scala.reflect.ClassTag

case class OperationBuilder(summary: String,
                            desc: String,
                            tags: Iterable[String],
                            id: String) {

  private val parameters = ArrayBuffer[Param[_]]()

  private var _requestBody: RequestBodyBuilder[_] = _

  private val _responses = ArrayBuffer[Produce[_]]()

  private var _consumes: Iterable[Consume[_]] = _

  def params(param: Param[_]*): OperationBuilder = {
    parameters ++= param
    this
  }

  def requestBody[T: ClassTag](desc: String = "", mimeType: String = "", consumes: Iterable[Consume[_]] = Nil): OperationBuilder = {
    _consumes = consumes
    _requestBody = new RequestBodyBuilder[T](desc, mimeType)
    this
  }

  def responses(res: Produce[_]*): OperationBuilder = {
    _responses ++= res
    this
  }

  def build(openAPI: OpenAPI): Operation = {
    val components = openAPI.getComponents
    val op = new Operation
    op.setSummary(summary)
    for (t <- tags) {
      op.addTagsItem(t)
    }
    op.setDescription(desc)
    val responses = new ApiResponses
    for (res <- _responses) {
      responses.addApiResponse(res.name, res.build(components))
    }
    val opParameters = new util.ArrayList[Parameter]()
    for (p <- parameters) {
      val pp = p.build(components)
      for (pppp <- pp) {
        opParameters.add(pppp)
      }
    }
    op.parameters(opParameters)
    if (_requestBody != null) {
      val requestBody = new RequestBody()
      _requestBody.build(components, requestBody)
      requestBody.content(buildContent(components))
      op.setRequestBody(requestBody)
    }
    op.setResponses(responses)
    op
  }

  private def buildContent(components: Components): Content = {
    if (_consumes != null) {
      val content = new Content
      for (p <- _consumes) {
        content.addMediaType(p.mimeType, p.build(components))
      }
      content
    } else null
  }

}
