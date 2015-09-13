package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessHandler;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

final class ReactiveProcessHandler<T> implements NuProcessHandler {

    private final Subject<ByteBuffer, ByteBuffer> stdout = PublishSubject.create();

    private final Subject<ByteBuffer, ByteBuffer> stderr = PublishSubject.create();

    private final Subject<Integer, Integer> exitCode = AsyncSubject.create();

    private final ReactiveDecoder decoder;

    private final Observer<ProcessOutput<T>> stdoutObserver;

    private final Observer<ProcessOutput<T>> stderrObserver;

    private ReactiveProcess process = null;

    public ReactiveProcessHandler(
            ReactiveDecoder<T> decoder,
            Observer<ProcessOutput<T>> stdoutObserver,
            Observer<ProcessOutput<T>> stderrObserver) {
        this.decoder = decoder;
        this.stdoutObserver = stdoutObserver;
        this.stderrObserver = stderrObserver;
    }

    public ReactiveProcess process() {
        return this.process;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onPreStart(final NuProcess nuProcess) {
        this.process = new DefaultReactiveProcess(nuProcess, exitCode);
        Observable.Operator<ProcessOutput<T>, T> combineWithProcess = combineWith(process);

        // subscribe to stdout
        if (stdoutObserver != null) {
            Observable<T> out;
            Observable.Operator<T, ByteBuffer> outDecoder = decoder.create();
            if (outDecoder == null) {
                out = (Observable<T>) this.stdout; // T must be ByteBuffer
            } else {
                out = stdout.lift(outDecoder);
            }
            out.lift(combineWithProcess).subscribe(stdoutObserver);
        }

        // subscribe to stderr
        if (stderrObserver != null) {
            Observable<T> err;
            Observable.Operator<T, ByteBuffer> errDecoder = decoder.create();
            if (errDecoder == null) {
                err = (Observable<T>) this.stderr; // T must be ByteBuffer
            } else {
                err = stderr.lift(errDecoder);
            }
            err.lift(combineWithProcess).subscribe(stderrObserver);
        }
    }

    private static <T> Observable.Operator<ProcessOutput<T>, T> combineWith(ReactiveProcess p) {
        return new Observable.Operator<ProcessOutput<T>, T>() {
            @Override
            public Subscriber<? super T> call(Subscriber<? super ProcessOutput<T>> op) {
                return new Subscriber<T>() {
                    @Override
                    public void onNext(T data) {
                        op.onNext(new ProcessOutput<T>(p, data));
                    }
                    @Override
                    public void onCompleted() {
                        op.onCompleted();
                    }
                    @Override
                    public void onError(Throwable e) {
                        op.onError(e);
                    }
                };
            }
        };
    }

    @Override
    public void onStart(final NuProcess nuProcess) {
        // do nothing
    }

    @Override
    public boolean onStdinReady(ByteBuffer buffer) { return false; }

    @Override
    public void onStdout(ByteBuffer buffer, boolean closed) {
        if (!closed) {
            stdout.onNext(buffer);
        } else {
            stdout.onCompleted();
        }
    }

    @Override
    public void onStderr(ByteBuffer buffer, boolean closed) {
        if (!closed) {
            stderr.onNext(buffer);
        } else {
            stderr.onCompleted();
        }
    }

    @Override
    public void onExit(int exitCode) {
        stdout.onCompleted();
        stderr.onCompleted();
        this.exitCode.onNext(exitCode);
        this.exitCode.onCompleted();
    }

}
