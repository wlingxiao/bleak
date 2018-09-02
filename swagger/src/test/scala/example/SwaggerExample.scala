package example

import java.nio.ByteBuffer

import goa._
import goa.netty.NettyHttpServer
import goa.swagger2._
import io.swagger.annotations.ApiModel

@ApiModel(description = "user param")
case class SimpleUser(@ApiModelProperty(value = "用户Id") id: Long,
                      @ApiModelProperty(value = "用户名") name: String)

class SwaggerExample extends Controller {

  val getUserById = get("/users/{id}") { ctx =>
    println(ctx.request.cookies)
    ctx.ok()
      .contentType("text/plain")
      .cookie(Cookie("username", "password", "localhost"))
      .body(Buf(ctx.request.params.get("id").get.getBytes()))
  }

  val createUser = post("/users") { ctx =>
    ctx.ok()
      .header("aa", "bb")
      .body()
  }


  private implicit val api: Api = Api(produces = "application/json", tags = Seq("用户"))

  doc(getUserById).apiOperation(ApiOperation("获取所有用户", response = classOf[SimpleUser])).apiParam(PathParam[SimpleUser]("id", desc = "用户id", required = true))
  doc(createUser).apiOperation("新建用户").apiParam(BodyParam[SimpleUser](desc = "用户信息"))

}

object SwaggerExample {

  def main(args: Array[String]): Unit = {

    val app: Goa = new Goa with NettyHttpServer
    app.mount(new SwaggerExample)
    app.use(new SwaggerModule(apiConfig))
    app.use(new AccessLogMiddleware)
    app.run()

    def apiConfig: ApiConfig = {
      ApiConfig(basePath = "")
    }
  }
}
