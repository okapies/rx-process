package com.github.okapies.rx.process;

public interface ReactiveProcessBuilder<T> {

    ReactiveProcessBuilder and(ReactiveProcessBuilder other);

    ReactiveProcessBuilder or(ReactiveProcessBuilder other);

    ReactiveProcessBuilder then(ReactiveProcessBuilder other);

    ReactiveProcessBuilder pipe(ReactiveProcessBuilder other);

    ReactiveProcess run();

    ReactiveProcess run(ProcessObserver<T> observer);

}
