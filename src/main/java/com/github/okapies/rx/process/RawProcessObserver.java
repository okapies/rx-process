package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import rx.Observable;
import rx.Observer;

public class RawProcessObserver extends AbstractProcessObserver<ByteBuffer> {

    public RawProcessObserver() {
        this(null, null);
    }

    public RawProcessObserver(Observer<ByteBuffer> stdout, Observer<ByteBuffer> stderr) {
        super(stdout, stderr);
    }

    public RawProcessObserver stdout(Observer<ByteBuffer> o) {
        return new RawProcessObserver(o, this.stderr);
    }

    public RawProcessObserver stderr(Observer<ByteBuffer> o) {
        return new RawProcessObserver(this.stdout, o);
    }

    @Override
    public Observable.Operator<ByteBuffer, ByteBuffer> decoder() {
        return null; // `null` indicates no operators are applied to output streams.
    }

}
