bleak is a micro web framework for scala based on Java nio.

#### Getting started
```scala
import bleak._

class GreetingsController extends Controller {
    get("/greetings") {ctx =>
        ctx.body("Hello World")
    }
}

import bleak.netty._

class HelloWorld extends App {
    val app = new bleak with NettyHttpServer
   app.mount(new GreetingsController)
    // running on http://localhost:7856
    app.run()
}
```
Heading over to `http://localhost:7856/greetings`,and you should see a greeting message. 

#### Routes

```scala
import bleak._
class MyController extends Controller {

    get("/") {ctx =>
        // Show something
    }

    post("/") {ctx =>
        // Create something
    }
}
```

Route patten can include named parameters,accessible via the `ctx.request.params`
```scala
// matches "GET /users/1" and "GET /users/2"
// ctx.request.params.get("id") is '1' or '2'
get("/users/{id}") {ctx =>
    ctx.request.params.get("id")
}
```

Route path can also include wildcard parameters.These parameters can be accessed via the `ctx.request.params("splat")`
```scala
// matches "GET /say/hello/to/world"
// ctx.request.params("splat") will return Seq('hello', 'world')
get("/say/*/to/*") {ctx =>
    ctx.request.params("splat")
}
```

#### Request
Request information cab be accessed via the `ctx.request`
```scala
val request = ctx.request

request.version // the HTTP version (1.0, 1.1)
request.method // the HTTP method (GET, POST, etc..)
request.uri // the uri e.g. /example?name=test
request.path // the path e.g. /example

request.remoteAddress // Remote InetSocketAddress
request.remoteHost // the host name of the client
request.remotePort // the IP source port of the client

request.localAddress
request.localeHost
request.localPort

request.headers // get all request headers
request.cookies // get all request cookies

request.body // get request body sent by the client

```
