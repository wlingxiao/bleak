package goa


class Cookie private(
                      private[this] var _name: String,
                      private[this] var _value: String,
                      private[this] var _domain: String,
                      private[this] var _path: String,
                      private[this] var _maxAge: Long,
                      private[this] var _secure: Boolean,
                      private[this] var _httpOnly: Boolean,
                    ) {


  def name: String = _name

  def value: Option[String] = Option(_value)

  def domain: Option[String] = Option(_domain)

  def path: Option[String] = Option(_path)

  def maxAge: Long = _maxAge

  def secure: Boolean = _secure

  def httpOnly: Boolean = _httpOnly

  private[this] def copy(
                          name: String = _name,
                          value: String = _value,
                          domain: String = _domain,
                          path: String = _path,
                          maxAge: Long = _maxAge,
                          secure: Boolean = _secure,
                          httpOnly: Boolean = _httpOnly
                        ): Cookie =
    new Cookie(
      name,
      value,
      domain,
      path,
      maxAge,
      secure,
      httpOnly
    )

  def domain(domain: String): Cookie =
    copy(domain = domain)

  def maxAge(maxAge: Long): Cookie =
    copy(maxAge = maxAge)

  def path(path: String): Cookie =
    copy(path = path)

  def httpOnly(httpOnly: Boolean): Cookie =
    copy(httpOnly = httpOnly)

  def secure(secure: Boolean): Cookie =
    copy(secure = secure)

  override def toString: String = {
    val buf = StringBuilder.newBuilder
      .append(name)
      .append('=')
      .append(value.getOrElse(""))
    domain.foreach(buf.append(", domain=").append(_))
    path.foreach(buf.append(", path=").append(_))
    if (maxAge >= 0) {
      buf.append(", maxAge=")
        .append(maxAge)
        .append('s')
    }
    if (secure) {
      buf.append(", secure")
    }
    if (httpOnly) {
      buf.append(", HTTPOnly")
    }
    buf.toString()
  }

}

object Cookie {

  def apply(name: String,
            value: String,
            domain: String = null,
            path: String = null,
            maxAge: Long = Long.MinValue,
            secure: Boolean = false,
            httpOnly: Boolean = false):
  Cookie = {
    new Cookie(name, value, domain, path, maxAge, secure, httpOnly)
  }

}
