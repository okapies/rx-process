package com.github.okapies.rx.process;

import rx.Observer;

public class ProcessObserver<T> {

    private final Observer<T> stdout;

    private final Observer<T> stderr;

    public ProcessObserver() {
        this(null, null);
    }

    public ProcessObserver(Observer<T> stdout, Observer<T> stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public Observer<T> stdout() {
        return this.stdout;
    }

    public ProcessObserver<T> stdout(Observer<T> stdout) {
        return new ProcessObserver<>(stdout, this.stderr);
    }

    public Observer<T> stderr() {
        return this.stderr;
    }

    public ProcessObserver<T> stderr(Observer<T> stderr) {
        return new ProcessObserver<>(this.stderr, stderr);
    }

}
