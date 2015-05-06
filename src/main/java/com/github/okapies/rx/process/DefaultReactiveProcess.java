package com.github.okapies.rx.process;

import com.zaxxer.nuprocess.NuProcess;

import rx.Observable;
import rx.Observer;

class DefaultReactiveProcess<T> implements ReactiveProcess<T> {

    private final NuProcess process;

    private final Observer<T> stdin;

    private final Observable<T> stdout;

    private final Observable<T> stderr;

    private final Observable<Integer> exitCode;

    public DefaultReactiveProcess(
            NuProcess process,
            Observer<T> stdin,
            Observable<T> stdout,
            Observable<T> stderr,
            Observable<Integer> exitCode) {
        this.process = process;
        this.stdin = stdin;
        this.stdout = stdout;
        this.stderr = stderr;
        this.exitCode = exitCode;
    }

    @Override
    public Observer<T> stdin() {
        return this.stdin;
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
