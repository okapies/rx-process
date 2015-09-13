package com.github.okapies.rx.process;

import java.nio.ByteBuffer;

import rx.Observable;

public interface ReactiveDecoder<T> {

    Observable.Operator<T, ByteBuffer> create();

}
