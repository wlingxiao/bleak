package goa

import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._

class QueryParamsTests extends BaseTests with MockitoSugar {
  private val QueryParamDecoder = Params.QueryParamDecoder

  test("QueryParamDecoder should decode query string from uri") {
    val uri = "http://localhost/test?name=user&age=10"
    val params = QueryParamDecoder.decode(uri)
    params("name")(0) shouldEqual "user"
    params("age")(0) shouldEqual "10"

    val multiParamUri = "http://localhost/test?person=one&person=two&sex=male"
    val multiParam = QueryParamDecoder.decode(multiParamUri)
    multiParam("person")(0) shouldEqual "one"
    multiParam("person")(1) shouldEqual "two"
    multiParam("sex")(0) shouldEqual "male"
  }

  test("QueryParam.splat should return None") {
    val request = mock[Request]
    when(request.uri).thenReturn("/test")
    when(request.mediaType).thenReturn(None)
    val queryParam = new Params.QueryParams(request)

    queryParam.splat shouldEqual None

  }

  test("QueryParam.get should return one value for the key") {
    val request = mock[Request]
    when(request.uri).thenReturn("/test?name=one&name=two")
    when(request.mediaType).thenReturn(None)
    val queryParam = new Params.QueryParams(request)

    queryParam.get("name") shouldEqual Some("one")
  }

  test("QueryParam.get should return all values for the key") {
    val request = mock[Request]
    when(request.uri).thenReturn("/test?name=one&name=two")
    when(request.mediaType).thenReturn(None)
    val queryParam = new Params.QueryParams(request)

    queryParam.getAll("name") shouldEqual Seq("one", "two")
  }

  test("QueryParam should get values from  encoded body") {
    val request = mock[Request]
    val buf = Buf("age=10&name=three".getBytes())
    when(request.uri).thenReturn("/test?name=one&name=two")
    when(request.charset).thenReturn(None)
    when(request.mediaType).thenReturn(Some(MediaType.WwwForm))
    when(request.body).thenReturn(buf)

    val queryParam = new Params.QueryParams(request)
    queryParam.get("age") shouldEqual Some("10")
    queryParam.getAll("name") shouldEqual Seq("one", "two", "three")
    queryParam.splat shouldEqual None
  }
}
