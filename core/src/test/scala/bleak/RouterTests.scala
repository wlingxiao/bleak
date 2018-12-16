package bleak

class RouterTests extends BaseTests {

  private val router = new Router {}

  import Route._

  test("Adds route for get") {
    router.get("/test", "test") {}
    val route = router.routes.head

    route.path shouldEqual "/test"
    route.methods shouldEqual Seq(Method.Get)
    route.name shouldEqual "test"
    route.attr[Consume] shouldEqual Some(router.consume)
    route.attr[Produce] shouldEqual Some(router.produce)
    route.attr[Charset] shouldEqual Some(router.charset)
  }

}
