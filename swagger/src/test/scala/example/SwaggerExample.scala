package example

import java.nio.ByteBuffer

import goa._
import goa.swagger2._

class SwaggerExample extends Controller {

  val getUserById = get("/users/{id}") { ctx =>
    ctx.ok().body(ByteBuffer.wrap("Hee".getBytes()))
  }

  val createUser = post("/users") { ctx =>
    ctx.ok()
  }


  private implicit val api: Api = Api(produces = "application/json", tags = Seq("用户"))

  doc(getUserById).apiOperation("获取所有用户").apiParam(PathParam("id"))
  doc(createUser).apiOperation("新建用户")

}

object SwaggerExample extends App {
  val app = Goa()
  app.mount(new SwaggerExample)
  app.use(new SwaggerModule(apiConfig))
  app.run()

  def apiConfig: ApiConfig = {
    ApiConfig(basePath = "")
  }
}
