package goa.server
package http1

case class HttpRequest(
                        method: String,
                        url: String,
                        majorVersion: Int,
                        minorVersion: Int,
                        headers: Seq[(String, String)],
                        body: BodyReader
                      ) {

  def contentType: String = {
    headers.filter { x =>
      x._1.equalsIgnoreCase(HeaderNames.ContentType)
    }.head._2
  }
}
