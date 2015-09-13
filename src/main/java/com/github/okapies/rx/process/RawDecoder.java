package com.github.okapies.rx.process;

import rx.Observable;

import java.nio.ByteBuffer;

public class RawDecoder implements ReactiveDecoder<ByteBuffer> {

    private static final ReactiveDecoder<ByteBuffer> INSTANCE = new RawDecoder();

    public static ReactiveDecoder<ByteBuffer> instance() {
        return INSTANCE;
    }

    @Override
    public Observable.Operator<ByteBuffer, ByteBuffer> create() {
        return null; // do nothing
    }

}
