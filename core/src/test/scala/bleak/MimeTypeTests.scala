package bleak

class MimeTypeTests extends BaseTests {

  test("get mime type from file name") {
    val jsFile = "/abc/test.js"
    val mimeType = MimeType(jsFile)
    mimeType shouldEqual "application/javascript"

    val tarGzFile = "/abc/test.tar.gz"
    val tarGz = MimeType(tarGzFile)
    tarGz shouldEqual "application/gzip"

    val notFoundFile = "/abc/test.not.found"
    val notFound = MimeType(notFoundFile)
    notFound shouldEqual MimeType.DefaultMimeType

  }

}
