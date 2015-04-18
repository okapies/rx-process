package com.github.okapies.rx.process;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import rx.Observer;

public abstract class ReactiveProcessBuilder<T> {

    protected final List<String> command;

    protected final Observer<T> stdout;

    protected final Observer<T> stderr;

    protected ReactiveProcessBuilder(List<String> command, Observer<T> stdout, Observer<T> stderr) {
        this.command = command;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public List<String> command() {
        return this.command;
    }

    public ReactiveProcessBuilder<T> command(List<String> command) {
        return copy(command, this.stdout, this.stderr);
    }

    public ReactiveProcessBuilder<T> command(String... command) {
        return copy(Arrays.asList(command), this.stdout, this.stderr);
    }

    public Observer<T> stdout() {
        return this.stdout;
    }

    public ReactiveProcessBuilder<T> stdout(Observer<T> o) {
        return copy(this.command, o, this.stderr);
    }

    public Observer<T> stderr() {
        return this.stderr;
    }

    public ReactiveProcessBuilder<T> stderr(Observer<T> o) {
        return copy(this.command, this.stdout, o);
    }

    public abstract ReactiveProcessBuilder<T> copy(List<String> command, Observer<T> stdout, Observer<T> stderr);

    public abstract ReactiveProcess<T> start();

    public static ReactiveProcessBuilder<ByteBuffer> builder(List<String> command) {
        return new DefaultReactiveProcessBuilder(command, null, null);
    }

    public static ReactiveProcessBuilder<ByteBuffer> builder(String... command) {
        return builder(Arrays.asList(command));
    }

}
