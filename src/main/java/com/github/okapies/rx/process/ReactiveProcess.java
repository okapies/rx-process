package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import rx.Observable;

public interface ReactiveProcess<T> {

    void writeStdin(ByteBuffer buffer);

    void closeStdin();

    Observable<T> stdout();

    Observable<T> stderr();

    Observable<Integer> exitCode();

    void destroy();

    boolean isRunning();

}
