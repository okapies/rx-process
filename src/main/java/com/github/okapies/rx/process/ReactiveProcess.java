package com.github.okapies.rx.process;

import rx.Observable;
import rx.Observer;

public interface ReactiveProcess<T> {

    Observer<T> stdin();

    Observable<T> stdout();

    Observable<T> stderr();

    Observable<Integer> exitCode();

    void destroy();

    boolean isRunning();

}
