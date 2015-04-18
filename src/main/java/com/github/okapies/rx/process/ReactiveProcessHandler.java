package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessHandler;

import rx.Observable;
import rx.Observer;
import rx.functions.Action1;
import rx.subjects.AsyncSubject;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

final class ReactiveProcessHandler implements NuProcessHandler {

    private final Observable<ByteBuffer> inSubject;
    private final Subject<ByteBuffer, ByteBuffer> outSubject = PublishSubject.<ByteBuffer>create();
    private final Subject<ByteBuffer, ByteBuffer> errSubject = PublishSubject.<ByteBuffer>create();
    private final Subject<Integer, Integer> exitCodeSubject = AsyncSubject.<Integer>create();

    public ReactiveProcessHandler(Observable<ByteBuffer> inSubject) {
        this.inSubject = inSubject;
    }

    public Observable<ByteBuffer> stdin() {
        return this.inSubject;
    }

    public Observable<ByteBuffer> stdout() {
        return this.outSubject;
    }

    public Observable<ByteBuffer> stderr() {
        return this.errSubject;
    }

    public Observable<Integer> exitCode() {
        return this.exitCodeSubject;
    }

    public void onStart(final NuProcess nuProcess) {
        inSubject.subscribe(new Action1<ByteBuffer>() {
            public void call(ByteBuffer buf) {
                nuProcess.writeStdin(buf);
            }
        });
    }

    public boolean onStdinReady(ByteBuffer buffer) { return false; }

    public void onStdout(ByteBuffer buffer) {
        if (buffer != null) {
            outSubject.onNext(buffer);
        } else {
            outSubject.onCompleted();
        }
    }

    public void onStderr(ByteBuffer buffer) {
        if (buffer != null) {
            errSubject.onNext(buffer);
        } else {
            errSubject.onCompleted();
        }
    }

    public void onExit(int exitCode) {
        exitCodeSubject.onNext(exitCode);
        exitCodeSubject.onCompleted();
    }

}
