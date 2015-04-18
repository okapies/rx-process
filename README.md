# rx-process
Process using ReactiveX `Observable`.

```scala
import rx.Observer
import com.github.okapies.rx.process._

val builder = ReactiveProcessBuilder.linesBuilder("ls", "-al")
val o = new Observer[String] {
  def onNext(t: String) = println("received: " + t)
  def onCompleted() = {}
  def onError(e: Throwable) = {}
}
builder.stdout(o).start()
```
