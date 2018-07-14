package goa.swagger

import java.util.{List => JList}

import goa.Route
import io.swagger.models.parameters.{Parameter, PathParameter, QueryParameter}

object ScalaSwaggerReader {

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
