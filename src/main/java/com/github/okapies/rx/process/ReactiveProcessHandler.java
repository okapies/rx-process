package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessHandler;

import rx.Observable;
import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

final class ReactiveProcessHandler<T> implements NuProcessHandler {

    private final Subject<ByteBuffer, ByteBuffer> stdout = PublishSubject.create();

    private final Observable<T> decodedStdout;

    private final Subject<ByteBuffer, ByteBuffer> stderr = PublishSubject.create();

    private final Observable<T> decodedStderr;

    private final Subject<Integer, Integer> exitCode = AsyncSubject.create();

    @SuppressWarnings("unchecked")
    private ReactiveProcessHandler(ReactiveDecoder<T> decoder) {
        Observable.Operator<T, ByteBuffer> outDecoder = decoder.create();
        if (outDecoder == null) {
            this.decodedStdout = (Observable<T>) this.stdout; // T must be ByteBuffer
        } else {
            this.decodedStdout = stdout.lift(outDecoder);
        }

        Observable.Operator<T, ByteBuffer> errDecoder = decoder.create();
        if (errDecoder == null) {
            this.decodedStderr = (Observable<T>) this.stderr; // T must be ByteBuffer
        } else {
            this.decodedStderr = stderr.lift(errDecoder);
        }
    }

    public static ReactiveProcessHandler<ByteBuffer> create() {
        return new ReactiveProcessHandler<>(null);
    }

    public static <T> ReactiveProcessHandler<T> create(ReactiveDecoder<T> decoder) {
        return new ReactiveProcessHandler<>(decoder);
    }

    public Observable<ByteBuffer> stdout() {
        return this.stdout;
    }

    public Observable<T> decodedStdout() {
        return this.decodedStdout;
    }

    public Observable<ByteBuffer> stderr() {
        return this.stderr;
    }

    public Observable<T> decodedStderr() {
        return this.decodedStderr;
    }

    public Observable<Integer> exitCode() {
        return this.exitCode.first();
    }

    @Override
    public void onPreStart(final NuProcess nuProcess) {
        // do nothing
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
