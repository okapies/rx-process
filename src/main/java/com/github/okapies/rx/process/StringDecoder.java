package com.github.okapies.rx.process;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import rx.Observable;

public class StringDecoder implements ReactiveDecoder<String> {

    private final Charset charset;

    public StringDecoder(Charset charset) {
        this.charset = charset;
    }

    public StringDecoder() {
        this.charset = Charset.defaultCharset();
    }

    public Charset charset() {
        return this.charset;
    }

    public StringDecoder charset(Charset charset) {
        return new StringDecoder(charset);
    }

    @Override
    public Observable.Operator<String, ByteBuffer> create() {
        return new OperatorByteBufferToString(charset);
    }

}
