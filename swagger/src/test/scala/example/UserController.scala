package example

import io.swagger.annotations._
import goa.annotation._
import goa._

import scala.annotation.meta.field
import scala.beans.BeanProperty

@ApiModel(description = "user param")
case class User(@BeanProperty @(ApiModelProperty@field)(value = "username") name: String,
                @BeanProperty @(ApiModelProperty@field)(value = "age") age: Long)

case class BadRequestResult(@BeanProperty @(ApiModelProperty@field)(value = "error message") msg: String)

@Api(tags = Array("user"))
@route("/users") class UserController {

  @ApiResponses(Array(
    new ApiResponse(code = 400, message = "bad request", response = classOf[BadRequestResult]),
    new ApiResponse(code = 500, message = "internal server error", response = classOf[BadRequestResult])))
  @ApiOperation(value = "filter users", produces = "application/json;utf-8")
  @get def filterUsers(@path id: Long): List[User] = {
    response.contentType = "application/json;utf-8"
    List(User("admin", 10)) // FIXME cannot parse scala generic type
  }

  @ApiOperation(value = "get user by user id", produces = "application/json;utf-8", response = classOf[User])
  @get("/{id}") def getUserById(@path id: Long): User = {
    User("admin", 10)
  }

  @ApiOperation(value = "create user", produces = "application/json;utf-8")
  @post def createUser(@body user: User): Int = {
    200
  }

}
