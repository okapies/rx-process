package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import rx.Observable;
import rx.Observer;

public class ByteBufferProcessObserver extends AbstractProcessObserver<ByteBuffer> {

    public ByteBufferProcessObserver() {
        this(null, null);
    }

    public ByteBufferProcessObserver(Observer<ByteBuffer> stdout, Observer<ByteBuffer> stderr) {
        super(stdout, stderr);
    }

    public ByteBufferProcessObserver stdout(Observer<ByteBuffer> o) {
        return new ByteBufferProcessObserver(o, this.stderr);
    }

    public ByteBufferProcessObserver stderr(Observer<ByteBuffer> o) {
        return new ByteBufferProcessObserver(this.stdout, o);
    }

    public Observable<ByteBuffer> serialize(Observable<ByteBuffer> o) {
        return o;
    }

    public ByteBuffer deserialize(ByteBuffer t) {
        return t;
    }

}
