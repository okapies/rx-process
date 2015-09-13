package com.github.okapies.rx.process;

public final class ProcessOutput<T> {

    private ReactiveProcess process;

    private T data;

    public ProcessOutput(ReactiveProcess process, T data) {
        this.process = process;
        this.data = data;
    }

    public ReactiveProcess process() {
        return this.process;
    }

    public T data() {
        return this.data;
    }

}
