package com.github.okapies.rx.process;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import rx.Observable;
import rx.Observer;

public class StringProcessObserver extends AbstractProcessObserver<String> {

    private final Charset charset;

    StringProcessObserver(Observer<String> stdout, Observer<String> stderr, Charset charset) {
        super(stdout, stderr);
        this.charset = charset;
    }

    public StringProcessObserver() {
        super(null, null);
        this.charset = Charset.defaultCharset();
    }

    public StringProcessObserver stdout(Observer<String> o) {
        return new StringProcessObserver(o, this.stderr, this.charset);
    }

    public StringProcessObserver stderr(Observer<String> o) {
        return new StringProcessObserver(this.stdout, o, this.charset);
    }

    public Charset charset() {
        return this.charset;
    }

    public StringProcessObserver charset(Charset charset) {
        return new StringProcessObserver(this.stdout, this.stderr, charset);
    }

    @Override
    public Observable.Operator<String, ByteBuffer> decoder() {
        return new OperatorByteBufferToString(charset);
    }

}
