package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessHandler;

import rx.Observable;
import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

final class ReactiveProcessHandler implements NuProcessHandler {

    private final Subject<ByteBuffer, ByteBuffer> outSubject = PublishSubject.create();
    private final Subject<ByteBuffer, ByteBuffer> errSubject = PublishSubject.create();
    private final Subject<Integer, Integer> exitCodeSubject = AsyncSubject.create();

    public Observable<ByteBuffer> stdout() {
        return this.outSubject;
    }

    public Observable<ByteBuffer> stderr() {
        return this.errSubject;
    }

    public Observable<Integer> exitCode() {
        return this.exitCodeSubject;
    }

    @Override
    public void onStart(final NuProcess nuProcess) {
        // do nothing
    }

    @Override
    public boolean onStdinReady(ByteBuffer buffer) { return false; }

    @Override
    public void onStdout(ByteBuffer buffer) {
        if (buffer != null) {
            outSubject.onNext(buffer);
        } else {
            outSubject.onCompleted();
        }
    }

    @Override
    public void onStderr(ByteBuffer buffer) {
        if (buffer != null) {
            errSubject.onNext(buffer);
        } else {
            errSubject.onCompleted();
        }
    }

    @Override
    public void onExit(int exitCode) {
        outSubject.onCompleted();
        errSubject.onCompleted();
        exitCodeSubject.onNext(exitCode);
        exitCodeSubject.onCompleted();
    }

}
