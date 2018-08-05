package example

import io.swagger.annotations._
import goa.annotation._
import goa._

import scala.annotation.meta.field
import scala.beans.BeanProperty

@ApiModel(description = "5464")
case class User(@BeanProperty @(ApiModelProperty@field)(value = "username") name: String,
                @BeanProperty @(ApiModelProperty@field)(value = "age") age: Long)

@Api(tags = Array("user"))
@route("/users") class UserController {

  @ApiResponses(Array(
    new ApiResponse(code = 200, message = "success", reference = "#/definitions/User"),
    new ApiResponse(code = 400, message = "bad request")))
  @ApiOperation(value = "filter users", produces = "application/json;utf-8", response = classOf[User])
  @get def filterUsers(@path id: Long): List[User] = {
    response.contentType = "application/json;utf-8"
    List(User("admin", 10))
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
