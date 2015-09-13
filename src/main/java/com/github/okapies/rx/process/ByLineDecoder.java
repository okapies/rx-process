package com.github.okapies.rx.process;

import rx.Observable;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import static com.github.okapies.rx.process.Util.compose;

public class ByLineDecoder implements ReactiveDecoder<String> {

    private final Charset charset;

    public ByLineDecoder(Charset charset) {
        this.charset = charset;
    }

    public ByLineDecoder() {
        this.charset = Charset.defaultCharset();
    }

    public Charset charset() {
        return this.charset;
    }

    public ByLineDecoder charset(Charset charset) {
        return new ByLineDecoder(charset);
    }

    @Override
    public Observable.Operator<String, ByteBuffer> create() {
        return compose(new OperatorSplitString("\r?\n"), new OperatorByteBufferToString(charset));
    }

}
