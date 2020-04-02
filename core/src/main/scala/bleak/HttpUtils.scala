package bleak

import io.netty.handler.codec.http.{HttpHeaderNames, HttpHeaderValues, HttpUtil, HttpVersion}

object HttpUtils {

  def setKeepAlive(version: HttpVersion, headers: Headers, keepAlive: Boolean): Headers =
    if (version.isKeepAliveDefault) {
      if (keepAlive) {
        headers.remove(HttpHeaderNames.CONNECTION)
      } else {
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE)
      }
    } else {
      if (keepAlive) {
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE)
      } else {
        headers.remove(HttpHeaderNames.CONNECTION)
      }
    }

  def isKeepAlive(version: HttpVersion, headers: Headers): Boolean =
    !headers.contains(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE, ignoreCase = true) && (
      version.isKeepAliveDefault || headers.contains(
        HttpHeaderNames.CONNECTION,
        HttpHeaderValues.KEEP_ALIVE,
        ignoreCase = true)
    )

  def isTransferEncodingChunked(headers: Headers): Boolean =
    headers.contains(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED, ignoreCase = true)

  def setTransferEncodingChunked(headers: Headers, chunked: Boolean): Headers =
    if (chunked) {
      headers
        .set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED)
        .remove(HttpHeaderNames.CONTENT_LENGTH)
    } else {
      val encodings = headers.getAll(HttpHeaderNames.TRANSFER_ENCODING)
      if (encodings.isEmpty) {
        headers
      } else {
        val values =
          encodings.dropWhile(value => HttpHeaderValues.CHUNKED.contentEqualsIgnoreCase(value))
        if (values.isEmpty) {
          headers.remove(HttpHeaderNames.TRANSFER_ENCODING)
        } else {
          headers.set(HttpHeaderNames.TRANSFER_ENCODING, values)
        }
      }
    }

}
