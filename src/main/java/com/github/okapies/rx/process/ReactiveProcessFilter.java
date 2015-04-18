package com.github.okapies.rx.process;

import rx.Observable;

public abstract class ReactiveProcessFilter<S, T> implements ReactiveProcess<T> {

    private final ReactiveProcess<S> underlying;

    public ReactiveProcessFilter(ReactiveProcess<S> underlying) {
        this.underlying = underlying;
    }

    protected abstract S serialize(T v);

    protected abstract Observable<T> deserialize(Observable<S> o);

    @Override
    public Observable<T> stdout() {
        return deserialize(underlying.stdout());
    }

    @Override
    public Observable<T> stderr() {
        return deserialize(underlying.stderr());
    }

    @Override
    public Observable<Integer> exitCode() {
        return underlying.exitCode();
    }

    @Override
    public void writeStdin(T t) {
        underlying.writeStdin(serialize(t));
    }

    @Override
    public void closeStdin() {
        underlying.closeStdin();
    }

    @Override
    public void destroy() {
        underlying.destroy();
    }

    @Override
    public boolean isRunning() {
        return underlying.isRunning();
    }

}
