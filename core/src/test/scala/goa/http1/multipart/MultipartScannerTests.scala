package goa.http1.multipart

import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

import goa.BaseTests
import goa.http1.{BodyReader, HttpRequest}

class MultipartScannerTests extends BaseTests {

  test("parse multipart request") {
    val headers = Seq("content-type" -> "multipart/form-data; boundary=--------------------------744661334412585608819073")
    val httpRequest = HttpRequest("GET", "/test", 1, 1, headers, new BodyReader {

      val body = ("----------------------------744661334412585608819073\r\n" + "Content-Disposition: form-data; name=\"description\"\r\n" + "\r\n" + "666666\r\n" + "----------------------------744661334412585608819073--\r\n").getBytes
      val buf = ByteBuffer.wrap(body)

      override def discard(): Unit = ???

      override def apply(): ByteBuffer = {
        buf
      }

      override def isExhausted: Boolean = ???
    })
    val request = new Request(httpRequest)
    val condition = new AtomicBoolean(false)

    val handler = new MultipartEntryHandler {
      override def handle(multipartEntry: MultipartEntry): Unit = {
        val contentDisposition = multipartEntry.getContentDisposition
        val name = contentDisposition.getDispositionParamUnquoted("name")
        val nIOReader = multipartEntry.getNIOReader
        nIOReader.notifyAvailable(new ReadHandler {
          override def onDataAvailable(): Unit = ???

          override def onError(t: Throwable): Unit = ???

          override def onAllDataRead(): Unit = {
            condition.set(true)
            val buf = new Array[Char](1024)
            nIOReader.read(buf)
            val s = new String(buf).trim
            s shouldEqual "666666"
          }
        })
      }
    }

    MultipartScanner.scan(request, handler, null)

    condition.get() shouldEqual true
  }

}
