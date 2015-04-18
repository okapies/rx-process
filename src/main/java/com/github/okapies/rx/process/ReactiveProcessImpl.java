package com.github.okapies.rx.process;

import com.zaxxer.nuprocess.NuProcess;

import rx.Observable;
import rx.Observer;

class ReactiveProcessImpl<T> implements ReactiveProcess<T> {

    private final NuProcess process;

    private final Observer<T> stdin;

    private final Observable<T> stdout;

    private final Observable<T> stderr;

    private final Observable<Integer> exitCode;

    public ReactiveProcessImpl(
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

    public Observable<T> stdout() {
        return stdout;
    }

    public Observable<T> stderr() {
        return stderr;
    }

    public Observable<Integer> exitCode() {
        return this.exitCode;
    }

    public void writeStdin(T t) {
        stdin.onNext(t);
    }

    public void closeStdin() {
        process.closeStdin();
    }

    public void destroy() {
        process.destroy();
    }

    public boolean isRunning() {
        return process.isRunning();
    }

}
