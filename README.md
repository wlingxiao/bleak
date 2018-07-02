#### 入门
```scala
import goa._
import java.nio.ByteBuffer

class HelloWorld extends App {
    val app = Goa()
    
    app.get("/greetings") {
       "Hello World"
    }
    
    app.run()
}
```