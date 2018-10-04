package goa

import goa.Response.Builder

trait ResponseBuilder {
  self: Context =>
  def ok(body: Buf): Response = {
    Response(body = body)
  }

  def ok(): Builder = {
    request.route.attr(Symbol("MediaType"))
      .map(x => new Builder().contentType(x.asInstanceOf[String]))
      .getOrElse(new Builder())
  }

  def notFound(): Response = {
    Response(status = Status.NotFound)
  }

}
