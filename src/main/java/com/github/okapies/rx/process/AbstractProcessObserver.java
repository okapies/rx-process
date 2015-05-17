package com.github.okapies.rx.process;

import rx.Observer;

public abstract class AbstractProcessObserver<T> implements ProcessObserver<T> {

    protected final Observer<T> stdout;

    protected final Observer<T> stderr;

    protected AbstractProcessObserver(Observer<T> stdout, Observer<T> stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }

    @Override
    public Observer<T> stdout() {
        return this.stdout;
    }

    @Override
    public Observer<T> stderr() {
        return this.stderr;
    }

}
