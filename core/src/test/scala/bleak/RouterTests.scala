package bleak

class RouterTests extends BaseTests {

  private val router = new Router {}

  import Meta._

  test("Adds route for get") {
    router.get("/test", "test") {}
    val route = router.routes.head

    route.path shouldEqual "/test"
    route.methods shouldEqual Seq(Method.Get)
    route.name shouldEqual "test"
    route.meta[Consume] shouldEqual Some(router.consume)
    route.meta[Produce] shouldEqual Some(router.produce)
    route.meta[Charset] shouldEqual Some(router.charset)
  }

}
