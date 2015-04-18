package com.github.okapies.rx.process;

import java.nio.ByteBuffer;
import java.util.List;

import com.zaxxer.nuprocess.NuProcess;
import com.zaxxer.nuprocess.NuProcessBuilder;

import rx.Observable;
import rx.Observer;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

class DefaultReactiveProcessBuilder extends ReactiveProcessBuilder<ByteBuffer> {

    public DefaultReactiveProcessBuilder(
            List<String> command, Observer<ByteBuffer> stdout, Observer<ByteBuffer> stderr) {
        super(command, stdout, stderr);
    }

    @Override
    public ReactiveProcessBuilder<ByteBuffer> copy(
            List<String> command, Observer<ByteBuffer> stdout, Observer<ByteBuffer> stderr) {
        return new DefaultReactiveProcessBuilder(command, stdout, stderr);
    }

    @Override
    public ReactiveProcess<ByteBuffer> start() {
        Subject<ByteBuffer, ByteBuffer> inSubject = PublishSubject.<ByteBuffer>create();
        ReactiveProcessHandler handler = new ReactiveProcessHandler(inSubject);

        Observable<ByteBuffer> outObserver = handler.stdout();
        if (stdout != null) {
            outObserver.subscribe(stdout);
        }
        Observable<ByteBuffer> errObserver = handler.stderr();
        if (stderr != null) {
            errObserver.subscribe(stderr);
        }
        Observable<Integer> exitCodeObserver = handler.exitCode();

        NuProcess process = new NuProcessBuilder(handler, command).start();

        return new ReactiveProcessImpl<ByteBuffer>(
                process,
                inSubject,
                outObserver,
                errObserver,
                exitCodeObserver);
    }

}
