package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import com.zaxxer.nuprocess.NuProcess;

import rx.Observable;

class DefaultReactiveProcess implements ReactiveProcess {

    private final NuProcess process;

    private final Observable<Integer> exitCode;

    public DefaultReactiveProcess(
            NuProcess process,
            Observable<Integer> exitCode) {
        this.process = process;
        this.exitCode = exitCode;
    }

    @Override
    public void writeStdin(ByteBuffer buffer) {
        process.writeStdin(buffer);
    }

    @Override
    public void closeStdin() {
        process.closeStdin();
    }

    @Override
    public Observable<Integer> exitCode() {
        return this.exitCode;
    }

    @Override
    public void destroy(boolean force) {
        process.destroy(force);
    }

    @Override
    public boolean isRunning() {
        return process.isRunning();
    }

}
