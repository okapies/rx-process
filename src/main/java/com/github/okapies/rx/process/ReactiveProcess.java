package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import rx.Observable;

public interface ReactiveProcess {

    void writeStdin(ByteBuffer buffer);

    void closeStdin();

    Observable<Integer> exitCode();

    void destroy(boolean force);

    boolean isRunning();

}
