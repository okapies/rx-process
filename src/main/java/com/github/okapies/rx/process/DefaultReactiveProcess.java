package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import com.zaxxer.nuprocess.NuProcess;

import rx.Observable;

class DefaultReactiveProcess<T> implements ReactiveProcess<T> {

    private final NuProcess process;

    private final Observable<T> stdout;

    private final Observable<T> stderr;

    private final Observable<Integer> exitCode;

    public DefaultReactiveProcess(
            NuProcess process,
            Observable<T> stdout,
            Observable<T> stderr,
            Observable<Integer> exitCode) {
        this.process = process;
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    @Override
    public void writeStdin(ByteBuffer buffer) {
        process.writeStdin(buffer);
    }

    @Override
    public void closeStdin() {
        process.closeStdin();
    }

    @Override
    public Observable<T> stdout() {
        return this.stdout;
    }

    @Override
    public Observable<T> stderr() {
        return this.stderr;
    }

    @Override
    public Observable<Integer> exitCode() {
        return this.exitCode;
    }

    @Override
    public void destroy() {
        process.destroy();
    }

    @Override
    public boolean isRunning() {
        return process.isRunning();
    }

}
