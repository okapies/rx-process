package com.github.okapies.rx.process;

import rx.Observable;

public interface ReactiveProcess<T> {

    Observable<T> stdout();

    Observable<T> stderr();

    Observable<Integer> exitCode();

    void writeStdin(T t);

    void closeStdin();

    void destroy();

    boolean isRunning();

}
