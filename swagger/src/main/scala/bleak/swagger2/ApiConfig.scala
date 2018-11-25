package bleak.swagger2

case class ApiConfig(schemes: Array[String] = null,
                     title: String = null,
                     version: String = null,
                     description: String = null,
                     termsOfServiceUrl: String = null,
                     contact: String = null,
                     license: String = null,
                     licenseUrl: String = null,
                     filterClass: String = null,
                     host: String = null,
                     basePath: String = null)
