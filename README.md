# rx-process
A reactive (non-blocking) `Process` API in Java/Scala using [NuProcess](https://github.com/brettwooldridge/NuProcess) and [ReactiveX](http://reactivex.io/).

## Settings and Limitations
rx-process inherits all the settings, performance characteristics, and limitations from *NuProcess*. See the [NuProcess's document](https://github.com/brettwooldridge/NuProcess) for more details.

## Usage
```scala
import rx.Observer
import com.github.okapies.rx.process._

val cmd = ReactiveCommand.build("ls", "-al").decoder(new ByLineDecoder)
val o = new Observer[String] {
  def onNext(t: String) = println(s"### onNext: '${t}' on ${Thread.currentThread().getName()}")
  def onCompleted() = println("### onCompleted")
  def onError(e: Throwable) = println(s"### onError: ${e}")
}
cmd.run(new ProcessObserver().stdout(o))
```
