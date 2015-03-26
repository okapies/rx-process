# paas
Process as a Service

```scala
import scala.sys.process._
import scala.concurrent.ExecutionContext.Implicits.global
import com.github.okapies.paas._

val f: Future[String] = Process("ls -l").??
f.foreach(println)
```
