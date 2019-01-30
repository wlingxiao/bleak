package bleak

import scala.concurrent.Future

abstract class Filter[-RequestIn, +ResponseOut, +RequestOut, -ResponseIn]
    extends ((RequestIn, Service[RequestOut, ResponseIn]) => Future[
      ResponseOut
    ]) {

  import Filter.AndThen

  def apply(request: RequestIn, service: Service[RequestOut, ResponseIn]): Future[ResponseOut]

  def andThen[Request2, Response2](
      next: Filter[RequestOut, ResponseIn, Request2, Response2]
  ): Filter[RequestIn, ResponseOut, Request2, Response2] =
    if (next eq Filter.identity)
      this.asInstanceOf[Filter[RequestIn, ResponseOut, Request2, Response2]]
    else AndThen(this, next, service => andThen(next.andThen(service)))

  def andThen(
      service: Service[RequestOut, ResponseIn]
  ): Service[RequestIn, ResponseOut] = {
    val svc = new Service.Proxy[RequestOut, ResponseIn](service) {}
    new Service[RequestIn, ResponseOut] {
      override def apply(request: RequestIn): Future[ResponseOut] =
        Filter.this.apply(request, svc)
    }
  }

}

private object Filter {

  def identity[Request, Response]: SimpleFilter[Request, Response] =
    Identity.asInstanceOf[SimpleFilter[Request, Response]]

  object Identity extends SimpleFilter[Any, Nothing] {
    override def andThen[Request2, Response2](
        next: Filter[Any, Nothing, Request2, Response2]
    ): Filter[Any, Nothing, Request2, Response2] = next

    def apply(request: Any, service: Service[Any, Nothing]): Future[Nothing] =
      service(request)
  }

  private case class AndThen[RequestIn, ResponseOut, RequestOut, ResponseIn](
      mw: Filter[_, _, _, _],
      andNext: Filter[_, _, _, _],
      build: Service[RequestOut, ResponseIn] => Service[RequestIn, ResponseOut]
  ) extends Filter[RequestIn, ResponseOut, RequestOut, ResponseIn] {
    override def andThen[Request2, Response2](
        next: Filter[RequestOut, ResponseIn, Request2, Response2]
    ): Filter[RequestIn, ResponseOut, Request2, Response2] =
      if (next eq identity)
        this.asInstanceOf[Filter[RequestIn, ResponseOut, Request2, Response2]]
      else AndThen(this, next, service => build(next.andThen(service)))

    override def andThen(
        service: Service[RequestOut, ResponseIn]
    ): Service[RequestIn, ResponseOut] = {
      val svc: Service[RequestIn, ResponseOut] = build(service)
      new Service.Proxy[RequestIn, ResponseOut](svc) {}
    }

    def apply(request: RequestIn, service: Service[RequestOut, ResponseIn]): Future[ResponseOut] =
      build(service)(request)
  }

}

private[bleak] trait SimpleFilter[Request, Response]
    extends Filter[Request, Response, Request, Response]
