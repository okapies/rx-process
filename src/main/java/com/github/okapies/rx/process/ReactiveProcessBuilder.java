package com.github.okapies.rx.process;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;

import rx.Observer;
import rx.Observable;

public class ReactiveProcessBuilder {

    private final List<String> command;

    private final Map<String, String> environment;

    public ReactiveProcessBuilder(List<String> command, Map<String, String> environment) {
        this.command = command;
        this.environment = environment;
    }

    public ReactiveProcessBuilder(List<String> command) {
        this(command, new TreeMap<String, String>(System.getenv()));
    }

    public ReactiveProcessBuilder(String... command) {
        this(Arrays.asList(command));
    }

    public List<String> command() {
        return this.command;
    }

    public ReactiveProcessBuilder command(List<String> command) {
        return new ReactiveProcessBuilder(command, this.environment);
    }

    public ReactiveProcessBuilder command(String... command) {
        return new ReactiveProcessBuilder(Arrays.asList(command), this.environment);
    }

    public Map<String, String> environment() {
        return this.environment;
    }

    public ReactiveProcessBuilder environment(Map<String, String> environment) {
        return new ReactiveProcessBuilder(this.command, environment);
    }

    public ReactiveProcessBuilder and(ReactiveProcessBuilder other) {
        return null; // TODO
    }

    public ReactiveProcessBuilder or(ReactiveProcessBuilder other) {
        return null; // TODO
    }

    public ReactiveProcessBuilder then(ReactiveProcessBuilder other) {
        return null; // TODO
    }

    public ReactiveProcessBuilder pipe(ReactiveProcessBuilder other) {
        return null; // TODO
    }

    @SuppressWarnings("unchecked")
    public <T> ReactiveProcess<T> start(ProcessObserver<T> observer) {
        ReactiveProcessHandler handler = new ReactiveProcessHandler();

        // stdout
        Observable<T> outObservable;
        Observable.Operator<T, ByteBuffer> decodeOut = observer.decoder();
        if (decodeOut == null) {
            // Note: T must be ByteBuffer
            outObservable = (Observable<T>) handler.stdout();
        } else {
            outObservable = handler.stdout().lift(decodeOut);
        }
        Observer<T> outObserver = observer.stdout();
        if (outObserver != null) {
            outObservable.subscribe(outObserver);
        }

        // stderr
        Observable<T> errObservable;
        Observable.Operator<T, ByteBuffer> decodeErr = observer.decoder();
        if (decodeErr == null) {
            // Note: T must be ByteBuffer
            errObservable = (Observable<T>) handler.stderr();
        } else {
            errObservable = handler.stderr().lift(decodeErr);
        }
        Observer<T> errObserver = observer.stderr();
        if (errObserver != null) {
            errObservable.subscribe(errObserver);
        }

        // exit code
        Observable<Integer> exitCodeObservable = handler.exitCode();

        // run an observed process
        NuProcessBuilder builder = new NuProcessBuilder(handler, command);
        builder.environment().clear();
        builder.environment().putAll(environment);
        NuProcess process = builder.start();

        return new DefaultReactiveProcess<T>(
                process,
                outObservable,
                errObservable,
                exitCodeObservable);
    }

    public ReactiveProcess<ByteBuffer> start() {
        return start(new RawProcessObserver());
    }

}
