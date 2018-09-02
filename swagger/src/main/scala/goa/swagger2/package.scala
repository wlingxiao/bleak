package goa

import scala.annotation.meta.field

package object swagger2 extends SwaggerSupport {

  type ApiModelProperty = io.swagger.annotations.ApiModelProperty@field


}
