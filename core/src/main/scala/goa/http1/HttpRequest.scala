package goa.http1

case class HttpRequest(
                        method: String,
                        url: String,
                        majorVersion: Int,
                        minorVersion: Int,
                        headers: Seq[(String, String)],
                        body: BodyReader
                      ) {

}
