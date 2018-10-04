package example

import goa._
import goa.netty.NettyHttpServer
import goa.swagger2._
import io.swagger.annotations.ApiModel

@ApiModel(description = "user param")
case class SimpleUser(@ApiModelProperty(value = "用户Id") id: Long,
                      @ApiModelProperty(value = "用户名") name: String)

class SwaggerExample extends Router {

  get("/users/{id}") { ctx =>
    println(ctx.request.remoteAddress)
    ctx.ok()
      .contentType("text/plain")
      .cookie(Cookie("username", "password", "localhost"))
      .body(Buf(ctx.request.params.get("id").get.getBytes()))
  }

  post("/users") { ctx =>
    ctx.ok()
      .header("aa", "bb")
      .body()
  }

  get("/users") { ctx =>
    ctx.ok().header("aaa", "bbb").body()
  }


  private implicit val api: Api = Api(produces = "application/json", tags = Seq("用户"))

  doc("GET /users/{id}")
    .operation(ApiOperation("获取所有用户", response = classOf[SimpleUser]))
    .param(PathParam[SimpleUser]("id", desc = "用户id", required = true))

  doc("POST /users")
    .operation("新建用户")
    .param(BodyParam[SimpleUser](desc = "用户信息"))

  doc("GET /users")
    .operation(ApiOperation("获取所有用户", response = classOf[SimpleUser]))
    .param(QueryParam[String]("username", "用户名"), QueryParam[Long]("age", "年龄"))
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
