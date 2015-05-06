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

    public <T> ReactiveProcess<T> start(ProcessObserver<T> observer) {
        ReactiveProcessHandler handler = new ReactiveProcessHandler();

        Observer<ByteBuffer> stdin = handler.stdin();
        Observer<T> inObserver = new Observer<T>() {
            public void onNext(T t) {
                stdin.onNext(observer.deserialize(t));
            }
            public void onCompleted() {
                stdin.onCompleted();
            }
            public void onError(Throwable e) {
                stdin.onError(e);
            }
        };

        Observable<T> outObservable = observer.serialize(handler.stdout());
        Observer<T> outObserver = observer.stdout();
        if (outObserver != null) {
            outObservable.subscribe(outObserver);
        }

        Observable<T> errObservable = observer.serialize(handler.stderr());
        Observer<T> errObserver = observer.stderr();
        if (errObserver != null) {
            errObservable.subscribe(errObserver);
        }

        Observable<Integer> exitCodeObservable = handler.exitCode();

        NuProcessBuilder builder = new NuProcessBuilder(handler, command);
        builder.environment().clear();
        builder.environment().putAll(environment);
        NuProcess process = builder.start();

        return new DefaultReactiveProcess<T>(
                process,
                inObserver,
                outObservable,
                errObservable,
                exitCodeObservable);
    }

    public ReactiveProcess<ByteBuffer> start() {
        return start(new ByteBufferProcessObserver());
    }

}
